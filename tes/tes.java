import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Scanner;
import javax.swing.plaf.SliderUI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

public class tes {

	// Definicao de variaveis

	static String topic;
	static int _tcpport = 59000;
	static int ecpPort = 58037;
	static int tesPort = 59000;
	static int userPort;
	static ArrayList<String> questionaires = new ArrayList<String>();
	static ArrayList<String> answers = new ArrayList<String>();
	static String ecpAddress = "localhost";
	static String delims = "[ |\n]";
	static String serverResponse = "";
	static String userSentence;
	static TreeMap<String, Integer> QIDS = new TreeMap<String, Integer>();
	static String[] readline;

	Random generator = new Random();
	static ServerSocket welcomesocket;

	public static void main(String[] args) throws Exception {
		int port = 58037;

		if (args.length > 0) {
			if (args.length == 2) {
				if (args[0].equals("-p")) {
					tesPort = Integer.parseInt(args[1]);
				} else if (args[0].equals("-n")) {
					ecpAddress = args[1];
				} else if (args[0].equals("-e")) {
					ecpPort = Integer.parseInt(args[1]);
				}
			}
			if (args.length == 4) {
				if (args[0].equals("-p")) {
					tesPort = Integer.parseInt(args[1]);
				} else if (args[0].equals("-n")) {
					ecpAddress = args[1];
				}

				if (args[2].equals("-n")) {
					ecpAddress = args[3];
				} else if (args[2].equals("-e")) {
					ecpPort = Integer.parseInt(args[3]);
				}
			}

			if (args.length == 6) {
				if (args[0].equals("-p")) {
					tesPort = Integer.parseInt(args[1]);
				}
				if (args[2].equals("-n")) {
					ecpAddress = args[3];
				}
				if (args[4].equals("-e")) {
					ecpPort = Integer.parseInt(args[5]);
				}
			}
		}

		try {
			welcomesocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("O porto escolhido nao esta disponivel.");
		}
		while (true) {
			Socket socket;
			try {
				socket = welcomesocket.accept();
				if (socket.isConnected()) {
					TCPServer thread = new TCPServer(socket);
					thread.start();
				}
			} catch (IOException e) {
				System.err.println("ERR: io exception");
			}
		}
	}

	static class TCPServer extends Thread {

		Socket socket;

		public TCPServer(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			try {
				readFile();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				userSentence = in.readLine();

				String[] token = userSentence.split(delims);

				if (token[0].equals("RQT")) {
					String SID = token[1];

					// Escolhe um ficheiro e carrega para a memoria

					Random random = new Random(1337);
					int index = random.nextInt(questionaires.size() - 1);

					File file = new File(questionaires.get(index) + ".pdf");

					long size = file.length();

					byte[] fileData = new byte[(int) file.length()];
					DataInputStream dis = new DataInputStream(new FileInputStream(file));
					dis.readFully(fileData);
					dis.close();

					// Obtem a altura do pedido e o tempo limite para ter resposta

					Date date = new Date();
					SimpleDateFormat ft = new SimpleDateFormat("ddMMMyyyy_hh:mm:ss");
					Date limit = new Date(date.getTime() + 60000);

					String QID = SID + "_" + ft.format(date);
					QIDS.put(QID, index);

					String limitTime = ft.format(limit);

					String response = "AQT " + QID + " " + limitTime + " " + size + " ";

					out.write(response.getBytes());
					out.write(fileData);
					out.write("\n".getBytes());
					out.close();

				}
				
				if (token[0].equals("RQS")) {

					String SID = token[1];
					String QID = token[2];

					String answer = answers.get(QIDS.get(QID));

					System.out.println(QID);
					int score = 0;
					for (int i = 0; i < 4; i++) {
						String aux = answer.charAt(0) + "";
						if (aux.equals(token[i + 3])) {
							score += 20;
						}
					}

					String responseUser = "AQS " + QID + " " + score + "\n";
					out.write(responseUser.getBytes());
					out.close();

					DatagramSocket userSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(ecpAddress);
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					String cmd = "IQR " + SID + " " + QID + " " + topic + " " + score + "\n";

					sendData = cmd.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, ecpPort);
					userSocket.send(sendPacket);

					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					userSocket.receive(receivePacket);
					String server_msg = new String(receivePacket.getData());

					System.out.println(server_msg);
					userSocket.close();

				}

			} catch (IOException e) {
				System.out.println("ERR: Nao se conseque receber pacotes do user");
				e.printStackTrace();
			}

		}

		public static void readFile() {

			try {
				Scanner scan = new Scanner(new FileInputStream("questionaires.txt"));
				topic = scan.nextLine();
				for (String in = scan.nextLine(); scan.hasNextLine(); in = scan.nextLine()) {
					String[] aux = in.split(" ");
					questionaires.add(aux[0]);
					answers.add(aux[1]);
				}
				scan.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}