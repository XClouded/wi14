package Host;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import data.Proj2Message;

public abstract class Proj2Node {
	protected static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	public static final int NODE_COUNT = PAXOS_MEMBERS.length;
	public static final int MAJORITY_SIZE = NODE_COUNT / 2 + 1;
	DatagramSocket msgSocket;
	private InetAddress IPAddress;

	protected Proj2Node() throws UnknownHostException {
		IPAddress = InetAddress.getByName("localhost");
	}

	/**
	 * Receive a Proj2Message on msgSocket
	 * @return the received Proj2Message
	 * @throws IOException
	 */
	Proj2Message receiveMessage() throws IOException {
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		msgSocket.receive(receivePacket);

		Proj2Message msg = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(receiveData);
		ObjectInput in = null;
		try {
			// convert the bytes back to a msg
			in = new ObjectInputStream(bis);
			Object o = in.readObject();
			msg = (Proj2Message)o;
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found exception, failed to cast message");
		} finally {
			try {
				bis.close();
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		return msg;
	}

	/**
	 * Send a Proj2Message to the specified node on msgSocket
	 * @param msg the Proj2Message to send
	 * @param to the node to send it to
	 * @throws IOException
	 */
	protected void sendMessage(Proj2Message msg, int to) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			// convert msg to bytes
			out = new ObjectOutputStream(bos);   
			out.writeObject(msg);
			byte[] bytes = bos.toByteArray();

			// send the message
			DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, to);
			msgSocket.send(sendPacket);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}
}
