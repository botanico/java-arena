package botanico.arena;

import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Bot {
	public static void main(String args[]) throws Exception {
		int arenaPort = parseInt(args[0]);
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] inbuf = new byte[1024];
		byte[] outbuf = new byte[1024];
		String sentence = "T00000N"+inFromUser.readLine();
		inbuf = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(inbuf, inbuf.length, IPAddress, arenaPort);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(outbuf, outbuf.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
	}
}
