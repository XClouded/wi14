package Host;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import data.LockAction;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;
import state.PaxosState;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {
	public static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	public static final int NODE_COUNT = PAXOS_MEMBERS.length;
	public static final int MAJORITY_SIZE = NODE_COUNT / 2 + 1;

	// make the private variables static, should be one PaxosNode per process
	private static TreeMap<Integer, PaxosState> roundState;
	public static int currentRound, clock, nid; 
	private static DatagramSocket msgSocket;
	private static InetAddress IPAddress;
	public static Queue<LockAction> requests;
	
	public static void main(String[] args) throws IOException {
		roundState = new TreeMap<Integer, PaxosState>();
		currentRound = 0; // for multi-paxos
		clock = 0; // for proposal #'s
		nid = 0;
		IPAddress = InetAddress.getByName("localhost");
		
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosNode 9001'");
			System.exit(1);
		}

		try {
			nid = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		msgSocket = new DatagramSocket(nid);
		
		System.out.println("PaxosNode started! Listening on port "+nid);

		// keep receiving messages
		while(true){
			Proj2Message msg = receiveMessage();
            
            // increment the local clock
            clock = Math.max(clock, msg.clockVal) + 1;
            
            if (msg.command == Command.LOCK_SERVICE_REQUEST){
            	//update the queue
            } else {
            	//Check the learner to see if the value has already been learned. 
            }
            PaxosState ps = roundState.get(currentRound);//TODO should replace this with the correct round info
            Proj2Message respMsg = null;
            //TODO handle each request based on the command
            switch(msg.command) {
            
	            case PREPARE:
	            case ACCEPT_REQUEST:
	        		if (!(msg.data instanceof PaxosMessage)){
	        			System.err.println("Acceptor: Received message data is not an "
	        					+ "instance of PaxosMessage");
	        			break;
	        		}
		            ps.acceptor.handleMessage(msg);
		            break;
	            case LOCK_SERVICE_REQUEST:
	            	//TODO 
	            case PROMISE:
	            case ACCEPTED:
	            	respMsg = ps.proposer.handleMessage(msg);
	            	break;
	            case LEARN:
	            	ps.learner.handleMessage(msg);
	            	break;
            	default:
            		System.out.println("Paxos got message: " + msg);
            }
            if(respMsg == null){
            	//error, ignore
            }
            //send response message
            if (respMsg.to == 0){
            	sendToAllPaxos(respMsg);
            }else{
            	sendMessage(respMsg, respMsg.to);
            }
            
            // TODO remove test code
            Proj2Message resp = new Proj2Message();
            resp.clockVal = clock;
            resp.from = nid;
            resp.command = Proj2Message.Command.LOCK_SERVICE_RESPONSE;
            
            sendMessage(resp, msg.from);
		}
	}
	
	/**
	 * Receive a Proj2Message on msgSocket
	 * @return the received Proj2Message
	 * @throws IOException
	 */
	private static Proj2Message receiveMessage() throws IOException {
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
	private static void sendMessage(Proj2Message msg, int to) throws IOException {
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
	
	public static void sendToAllPaxos(Proj2Message msg) throws UnknownHostException, IOException {
		for(int node : PAXOS_MEMBERS) {
			try {
				sendMessage(msg, node);
			} catch (ConnectException e) {
				// could not connect
				// node is down, ignore
			}
		}
	}
}
