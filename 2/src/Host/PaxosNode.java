package Host;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import state.PaxosState;
import data.LockAction;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode extends Proj2Node{
	private TreeMap<Integer, PaxosState> roundState;

	// static, because we assume one PaxosNode per process
	public static int currentRound;
	public static int nid, clock; 
	public static Queue<Proj2Message> requests;
	private static Map<String, Integer> heldLocks;

	public PaxosNode(int nodeId) throws UnknownHostException, SocketException {
		super();
		roundState = new TreeMap<Integer, PaxosState>();
		currentRound = 0; // for multi-paxos
		clock = 0; // for proposal #'s
		nid = nodeId;
		heldLocks = new HashMap<String, Integer>();
		requests = new LinkedList<Proj2Message>();
	}

	public void run() throws IOException {
		msgSocket = new DatagramSocket(nid);
		System.out.println("PaxosNode started! Listening on port "+nid);

		// keep receiving messages
		while(true){
			Proj2Message msg = receiveMessage();
			// increment the local clock
			clock = Math.max(clock, msg.clockVal) + 1;
			PaxosState ps = null;
			int round = 0;
//			System.out.println("command: " + msg.command);
			if (msg.command == Command.LOCK_SERVICE_REQUEST) {
				// msg is client request
				//update the queue
				LockAction la = (LockAction)msg.data;

				if (reentrantLockRequest(la)) {
					// this is a redundant request. Reply immediately...
					Proj2Message resp = new Proj2Message();
					resp.clockVal = clock;
					resp.from = nid;
					resp.command = Command.LOCK_SERVICE_RESPONSE;
					resp.data = la;
					sendMessage(resp, msg.from);

					// and wait for more requests
					continue;
				} else {
					requests.add(msg);

					if(roundState.isEmpty() || roundState.get(currentRound) == null 
							|| (roundState.get(currentRound).learner.learnedValue != null && canBeExecuted(la))) {
						// ready to execute request
						// increment the current round and go into the switch statement
						updateCurrentRound();

						round = currentRound;
					} else {
						// not ready to execute the request yet!
						continue;
					}
				}
			} else {
				// msg is from other server
				// if a value has already been learned, reply with the learned value
				PaxosMessage paxMsg = (PaxosMessage)msg.data;
				round = paxMsg.paxosRound;

				//            	else if (ps.learner.learnedValue != null) {
				//            		Proj2Message resp = new Proj2Message();
				//                	resp.clockVal = clock;
				//                	resp.from = nid;
				//                	resp.command = Command.LEARN;
				//            	}
			}

			ps = roundState.get(round);
			if(ps == null) {
				ps = new PaxosState();
				roundState.put(round, ps);
			} 

			switch(msg.command) {

			case PREPARE:
			case ACCEPT_REQUEST:
				if (!(msg.data instanceof PaxosMessage)){
					System.err.println("Acceptor: Received message data is not an "
							+ "instance of PaxosMessage");
					break;
				}
				List<Proj2Message> respMsgs = ps.acceptor.handleMessage(msg);
				if(respMsgs == null){
					//error, ignore
				}else{
					for(Proj2Message respMsg : respMsgs){
						sendMessage(respMsg, respMsg.to);
					}
				}
				break;
			case LOCK_SERVICE_REQUEST:
			case PROMISE:
			case ACCEPTED:
				Proj2Message respMsg = ps.proposer.handleMessage(msg);
				if(respMsg == null){
					//error, ignore
				}else{

					//send response message
					sendMessage(respMsg, respMsg.to);
				}
				break;
			case LEARN:
				Proj2Message learnerMsg = ps.learner.handleMessage(msg);
				
				if(learnerMsg == null){
					//there are three cases:
					//1. nothing learned. 
					//2. there is already a learned value
					//3. msg data is not an instace of PaxosMessage
				}else{
					LockAction la = ps.learner.learnedValue;
					if (la != null){
						valueLearned(la);
					}
					//send response message
					sendMessage(learnerMsg, learnerMsg.to);
				}
				break;
			default:
				System.out.println("Paxos got message: " + msg);
			}
		}
	}


	private void updateCurrentRound() {
		while(roundState.containsKey(currentRound)){
			currentRound ++;
		}
	}

	/*
	 * @param to the destination port. If set to -1, broadcast.
	 */
	protected void sendMessage(Proj2Message msg, int to) throws IOException{
		if(to == -1){
			for(int node : PAXOS_MEMBERS) {
				sendMessage(msg, node);
			}
		}else{
			super.sendMessage(msg, to);
		}
	}

	/**
	 * Returns true if this is a reentrant lock request, i.e. is
	 * a lock request for a lock already held by the client
	 * @param la
	 * @return
	 */
	private boolean reentrantLockRequest(LockAction la) {
		return la.lock
				&& heldLocks.containsKey(la.lockName)
				&& heldLocks.get(la.lockName) == la.client;
	}

	/**
	 * Returns true if the lock action can be safely executed.
	 * i.e. the request is for a lock that is not currently held,
	 * or is an unlock request for a lock currently held by the client
	 * @param la
	 * @return
	 */
	private boolean canBeExecuted(LockAction la) {
		if (la.lock) {
			return !heldLocks.containsKey(la.lockName);
		} else {
			return heldLocks.containsKey(la.lockName) 
					&& heldLocks.get(la.lockName) == la.client;
		}
	}

	/**
	 * This is called when a value is learned. 
	 * @param action The value being learned
	 * @throws IOException 
	 */
	public void valueLearned(LockAction action) throws IOException{
		boolean missionCompleted = false; //the task can be performed. 
		if(action.lock && 
				(!heldLocks.containsKey(action.lockName) || heldLocks.get(action.lockName) == action.client)){
			missionCompleted = true;
			//put the lock
			heldLocks.put(action.lockName, action.client);
		}else if(!(action.lock) && heldLocks.get(action.lockName) == action.client){
			missionCompleted = true;
			//unlock
			heldLocks.remove(action.lockName);
		}else {
			assert(false);
		}
		
		if(missionCompleted){
			// This means this value is proposed by the proposer in this server.
			// remove this action if exists
			int size = requests.size();
			for(int i = 0; i < size; i++){
				Proj2Message msg = requests.poll();
				if(((LockAction)msg.data).client == action.client){
					break;
				}else{
					requests.add(msg);
				}
			}
			size = requests.size();
			// propose the next value in the task queue. 
			for(int i = 0; i < size; i++){
				if (canBeExecuted((LockAction) requests.peek().data)){
					updateCurrentRound();
					PaxosState ps = new PaxosState();
					roundState.put(currentRound, ps);
					Proj2Message respMsg = ps.proposer.handleMessage(requests.peek());
					if(respMsg == null){
						//error, ignore
					}else{
						//send response message
						sendMessage(respMsg, respMsg.to);
					}
					break;
				}else {
					requests.add(requests.poll());
				}
			}
		}
		
	}
}
