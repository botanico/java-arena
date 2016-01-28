package botanico.arena;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * An arena is a UDP server managing the state of a game.
 *
 * @author jan
 */
public class Arena {

	public static void main(String args[]) throws IOException {
		new Arena(parseInt(args[0])).start();
	}

	// UDP
	private final int port;
	private final byte[] inBuf = new byte[64];
	private final byte[] outBuf = new byte[64];

	// bots
	private final String[] ips = new String[1000];
	private final int[] ports = new int[1000];
	private final String[] names = new String[1000];

	private final byte[] hps = new byte[1000];
	private final short[] xs = new short[1000];
	private final short[] ys = new short[1000];

	// game
	private int turn = 0;
	private boolean open = true;
	private int bots = 0;
	private int dead = 0;

	public Arena(int port) {
		super();
		this.port = port;
		// these will always be at the same index
		outBuf[0] = 'T'; // turn
		outBuf[6] = 'B'; // bot ID
		outBuf[10] = 'O'; // number of opponents left
		outBuf[14] = 'H'; // hit-points
		outBuf[17] = 'X'; // X position
		outBuf[21] = 'Y'; // Y position
	}

	public void start() throws IOException {
		try (DatagramSocket server = new DatagramSocket(port)) {
			System.out.println("Botanico@"+port);
			DatagramPacket inMsg = new DatagramPacket(inBuf, inBuf.length);
			while (true) {
				server.receive(inMsg);
				if (auth(inMsg)) {
					DatagramPacket outMsg = apply(inMsg);
					if (outMsg != null) {
						server.send(outMsg);
					}
				}
			}
		}
	}

	private boolean auth(DatagramPacket inMsg) {
		if (turn == 0) {
			if (decodeNumFromDigits(inBuf, 1, 5) != 0) {
				System.out.println("T[urn] must be zero to join");
				return false;
			}
			if (inBuf[7] != 'N') {
				System.out.println("N[ame] must be given to join");
				return false;
			}
		} else {
			int botID = decodeNumFromDigits(inBuf, 7, 3);
			if (ports[botID] != inMsg.getPort()
			|| !ips[botID].equals(inMsg.getAddress().getHostAddress())) {
				return false;
			}
		}
		return true;
	}

	private DatagramPacket apply(DatagramPacket inMsg) {
		int outLength = turn == 0
			? join(inMsg)
			: actions(inMsg);
		if (outLength == 0) {
			return null;
		}
		System.out.println("Response is:"+new String(outBuf, 0, outLength));
		return new DatagramPacket(outBuf, outLength, inMsg.getAddress(), inMsg.getPort());
	}

	private int actions(DatagramPacket inMsg) {
		int botID = decodeNumFromDigits(inBuf, 7, 3);

		outStatus(botID);
		return 25;
	}

	private int join(DatagramPacket inMsg) {
		bots++;
		outStatus(bots);
		ips[bots] = inMsg.getAddress().getHostAddress();
		ports[bots] = inMsg.getPort();
		names[bots] = new String(inBuf, 7, inMsg.getLength()-7);
		hps[bots] = 10;
		return 25;
	}

	private void outStatus(int botID) {
		encodeNumAsDigits(outBuf, 1, turn, 5);
		encodeNumAsDigits(outBuf, 7, bots, 3);
		encodeNumAsDigits(outBuf, 11, bots-1-dead, 3);
		encodeNumAsDigits(outBuf, 15, hps[botID], 2);
		encodeNumAsDigits(outBuf, 18, xs[botID], 3);
		encodeNumAsDigits(outBuf, 22, ys[botID], 3);
	}

	static void encodeNumAsDigits(byte[] buf, int offset, int num, int digits) {
		int pos = offset + digits - 1;
		for (int i = pos; i >= offset; i--) {
			buf[i] = (byte) ((num % 10) + '0');
			num /= 10;
		}
	}

	static int decodeNumFromDigits(byte[] buf, int offset, int digits) {
		int num = 0;
		for (int i = offset; i < offset+digits; i++) {
			num += buf[i] - '0';
			num *= 10;
		}
		num /= 10; // undo last times 10
		return num;
	}
}
