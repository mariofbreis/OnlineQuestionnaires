import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.ArrayList;
import java.nio.charset.Charset;

class UDPServer extends Thread {

	// Definicao de variaveis

	static int _udpport = 58037;
	static int userPort;
	static ArrayList<String> topics = new ArrayList<String>();
	static ArrayList<String> address = new ArrayList<String>();
	static String delims = "[ |\n]";
	static String serverResponse = "";
	static String[] receivedString, readline;

	Random generator = new Random();

	UDPServer(int port) {
		_udpport = port;
	}

	public static void readFile() {

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("topics.txt")));

			for (String in; (in = br.readLine()) != null;) {
				String[] aux = in.split(delims);
				topics.add(aux[0]);
				address.add(aux[1] + " " + aux[2]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		
		readFile();

		// Criacao do serverSocket UDP

		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(_udpport);
		} catch (SocketException e) {
			System.out.println("ERR: Nao consigo criar o serverSocket no port dado");
		}

		InetAddress userAddress;

		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		while (true) {

			// Criacao e recepcao dos pacotes do user em UDP

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.println("ERR: Nao se consegue receber pacotes do user");
			}
			userAddress = receivePacket.getAddress();
			userPort = receivePacket.getPort();
			String sentence = new String(receivePacket.getData());

			String userIP = userAddress.getHostAddress().toString();

			// Impressao do pedido do user em UDP

			receivedString = sentence.trim().split(delims);
			System.out.println(receivedString[0] + " " + userIP + " " + receivePacket.getPort());

			// Tratamento do pedido "list" TQR

			if (receivedString[0].equals("TQR")) {

				// Resposta do server ECP em UDP

				serverResponse = "AWT " + topics.size();

				if (!(topics.size() == 0)) {
					for (int i = 0; i < topics.size(); i++)
						serverResponse += " " + topics.get(i);
				}

				serverResponse += "\n";

				sendData = serverResponse.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, userAddress, userPort);

				try {
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("Nao se consegue enviar os pacotes de dados para o User");
				}

				readline = null;
			}

			if (receivedString[0].equals("TER")) {
				if (receivedString.length > 2) {
					serverResponse = "ERR";
					System.out.println(receivedString.length);
				} else {
					int nTopic = 1;
					try {
						nTopic = Integer.parseInt(receivedString[1].trim());
					} catch (NumberFormatException e) {
						System.out.println(receivedString[1]);
					}

					serverResponse = "AWTES " + address.get(nTopic - 1) + "\n";

					System.out.println(serverResponse);

					sendData = serverResponse.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, userAddress, userPort);

					try {
						serverSocket.send(sendPacket);
					} catch (IOException e) {
						System.out.println("Nao se consegue enviar os pacotes de dados para o ECP");
					}
					readline = null;
				}
			}

			if (receivedString[0].equals("IQR")) {

				if (receivedString.length > 5) {
					serverResponse = "ERR";
				} else {
					String QID = receivedString[2];
					serverResponse = "AWI " + " " + QID;

					sendData = serverResponse.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, userAddress, userPort);

					try {
						serverSocket.send(sendPacket);
					} catch (IOException e) {
						System.out.println("Nao se consegue enviar os pacotes de dados para o TES.");
					}

				}

			}
		}
	}
}

public class ecp {

	public static void main(String[] args) throws Exception {
		int port = 58037;

		if (args.length > 1 && args[0].equals("-p")) {
			port = Integer.parseInt(args[1]);
		}
		UDPServer udpconnection = new UDPServer(port);

		udpconnection.start();
	}
}