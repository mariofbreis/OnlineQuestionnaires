import java.io.*;
import java.net.*;
import java.util.Scanner;

public class user {

	// Definicao de variaveis

	static String input, cmd;
	static String address = "localhost"; 
	static String TESip = "localhost"; 
	static String delims = "[ \n]";
	static String[] tokens = {};
	static int port = 58037; 
	static int SID;
	static String IP;
	static int tesPort;
	static String QID;
	static String limit;
	static Socket tesSocket = null;

	public static void main(String args[]) throws IOException {

		Scanner br = new Scanner(System.in);

		// Preparacao para fazer a ligacao com o servidor
		// Comando: java user SID -n <address> -p <port>

		if (args.length > 0) {

			SID = Integer.parseInt(args[0]);
			// Testa a ausencia das opcoes -n e -p
			if (args.length == 3) {
				if (args[1].equals("-p")) {
					port = Integer.parseInt(args[2]);
				} else if (args[1].equals("-n")) {
					address = args[2];
				} else
					System.out.println("ERR: O user foi mal inicializado.");
			}

			if (args.length == 5) {
				if (args[1].equals("-n") && args[3].equals("-p")) {
					address = args[2];
					port = Integer.parseInt(args[4]);
				} else
					System.out.println("ERR: O user foi mal inicializado.");
			}

		} else
			System.out.println("ERR: O user foi mal inicializado.");

		for (String input = br.nextLine(); !input.equals("exit"); input = br.nextLine()) {
			tokens = input.split(delims);
			try {
				// Menu de comandos

				if (tokens[0].equals("list")) {
					list();
				}

				else if (tokens[0].equals("request")) {
					request();
				}

				else if (tokens[0].equals("submit")) {
					submit();
				}
			} catch (Exception e) {
				System.out.println("ERR: O user nao esta a funcionar correctamente.");
			}
		}
	}

	// Tratamento do List

	private static void list() {
		try {
			// Coneccao user em UDP para uso do comando list

			DatagramSocket userSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(address);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			cmd = "TQR\n";

			// User envia mensagem TQR ao ECP

			sendData = cmd.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			userSocket.send(sendPacket);

			// User recebe mensagem AWT do ECP

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			userSocket.receive(receivePacket);
			String server_msg = new String(receivePacket.getData());
			userSocket.close();

			// Parse da mensagem recebida do ECP

			String[] aux = server_msg.split(delims);

			if (aux[0].equals("AWT")) {
				int nTopics = Integer.parseInt(aux[1]);
				for (int i = 0; i < nTopics; i++) {
					System.out.println((i + 1) + "- " + aux[i + 2]);
				}
			} else if (aux[0].equals("ERR")) {
				System.out.println("ERR: O ECP nao conseguiu listar os topicos.");
			}
		} catch (Exception e) {
			System.err.println("ERR: " + e.toString());
		}

	}

	// Tratamento do Request

	private static void request() {
		try {
			// Coneccao user em UDP com o ECP para uso do comando request

			DatagramSocket userSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(address);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			cmd = "TER " + tokens[1] + "\n";

			// User envia mensagem TER ao ECP

			sendData = cmd.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			userSocket.send(sendPacket);

			// User recebe mensagem AWTES do ECP

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			userSocket.receive(receivePacket);
			String server_msg = new String(receivePacket.getData());
			userSocket.close();

			// Parse e Print da mensagem recebida do ECP

			String[] aux = server_msg.split(delims);
			System.out.println(aux[1] + " " + aux[2]);

			// Coneccao user em TCP com o TES para uso do comando request

			IP = aux[1];
			tesPort = Integer.parseInt(aux[2]);
			tesSocket = new Socket(IP, tesPort);

			// User envia mensagem RQT ao TES

			sendTCP(tesSocket, "RQT " + SID);

			byte[] resultBuff = new byte[0];
			byte[] buff = new byte[1375480];
			int t = -1;

			// Recebe a mensagem AQT em bytes

			BufferedInputStream in = new BufferedInputStream(tesSocket.getInputStream());

			while ((t = in.read(buff, 0, buff.length)) > -1) {
				byte[] tempBuff = new byte[resultBuff.length + t];
				System.arraycopy(resultBuff, 0, tempBuff, 0, resultBuff.length);
				System.arraycopy(buff, 0, tempBuff, resultBuff.length, t);
				resultBuff = tempBuff;
			}
			in.close();

			Scanner scan = new Scanner(new ByteArrayInputStream(resultBuff));

			// Parse da mensagem recebida do TES

			boolean validmsg = scan.next().equals("AQT");

			if (validmsg) {
				QID = scan.next();
				limit = scan.next();
				int size = scan.nextInt();
				scan.close();

				int offset = ("AQT " + QID + " " + limit + " " + size + " ").length();

				byte[] data = new byte[size];
				System.arraycopy(resultBuff, offset, data, 0, size);

				// Criacao do PDF com o nome do QID

				DataOutputStream out = new DataOutputStream(new FileOutputStream(QID + ".pdf"));
				out.write(data);
				out.close();
				File file = new File(QID + ".pdf");

				// User notificado do recebimento do ficheiro

				System.out.println("Received file " + QID + ".pdf");
				System.out.println(limit);

			} else
				System.out.println("Não foi possivel receber o ficheiro.");

			tesSocket.close();

		} catch (Exception e) {
			System.err.println("ERR: " + e.toString());
		}

	}

	// Tratamento do Submit

	private static void submit() {
		try {
			// Coneccao user em TCP com o TES para uso do comando submit

			String answer = tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + tokens[4] + " " + tokens[5];

			tesSocket = new Socket(IP, tesPort);

			// User envia mensagem RQS ao TES

			sendTCP(tesSocket, "RQS " + SID + " " + QID + " " + answer);

			Scanner scan = new Scanner(tesSocket.getInputStream());

			// Parse da mensagem AQS recebida do TES

			boolean validmsg = scan.next().equals("AQS");
		
			if (validmsg) {
				QID = scan.next();
				int score = scan.nextInt();
				scan.close();

				/*
				 * if(score == -1) {System.out.println("Negative Score");} else
				 * {
				 * 
				 * System.out.println(file.length() == size);
				 */
				System.out.println("Obtained score: " + score + "%");
				

			}
		} catch (Exception e) {
			System.err.println("ERR: O submit foi introduzido incorrectamente.");
		}
	}

	// Classe que trata do envio do user em TCP para o TES

	public static void sendTCP(Socket socket, String msg) {

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);

		} catch (IOException e) {
			System.err.println();
		}
	}
}
