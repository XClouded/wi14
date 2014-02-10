Jake Sanders 0728906 jacobs22@u.washington.edu
Pingyang He 0929017 pingyh@uw.edu

Design:
	(Un)Lock requests originate from a LockClient (instantiated via the main method in ClientMain). Once the client has started, commands take the form of 'lock x' 'unlock x' and 'exit'. Lock and unlock both block until the lock/unlock request has been granted.

	The state of all of the locks, the queue of pending lock requests, and the proposer, acceptor, and learner state machines for all instances of paxos are contained in the PaxosNodes. Each PaxonNode has a set of instances of paxos (multipaxos) represented by a Map<Integer, PaxosState> where the Integer is the round # and the PaxosState contains the Proposer, Acceptor, and Learner for that round. There is no specified leader for a given instance of paxos in our implementation. Any or all of the Proposers can initiate a vote for a givne round.
	
	The 'nodes' each occupy separate processes and communicate with each other via Java's datagram socket communication (UDP). This simplified the implementation, thanks to the lack of connections which created unnecessary complexity and possible deadlocks. This also eliminated some resiliance to finicky networks, but we decided to treat a dropped message in the same way as we treat any other kind of node failure. That is to say, with 5 nodes in the the group, Paxos can make progress even in the presence of 2 failures at each step. This resiliance naturally goes up as the paxos membership increases. Failures of redundant learners or acceptors do not require handling in order for paxos to make progress, and are not given special treatment in our implementation.
	
	When a server receives any kind of message, it first determines whether it is part of the paxos protocol or associated with the lock service. In the former case, the message is processed by that round's Proposer, Acceptor, or Learner, depending of whether it is an PREPARE, PROMISE, ACCEPT_REQUEST, ACCEPTED, or a LEARN request (defined in Proj2Message). In the latter case, pre-processing is done to determine whether the request can be processed immediately, or whether it needs to be queued pending an unlock request. Prepare and accept requests are handled by the Acceptor, promise and ACCEPTED are handled by the Proposer, and LEARN requests are handled by the Learner.

	The Proposer, Acceptor, and Learner state machines take in the message, process the contents internally, and return a new message with new receipient(s) in order to progress to the next stage of the protocol. The state machines can specify a single node to respond to (such as the client or reply to a proposer) or to broadcast to all of the paxos nodes. Since each node contains a proposer, acceptor, and a learner, this often means that messages are being sent straight back to the same node, which does not need any special handling thanks to the connectionless nature of the network.
	
	Whenever a value is learned for a round, the node also notifies the requesting client at its designated port. The client blocks until a response is received pertaining to its current request. This eliminates the case where a response accidentally gets mistaken for a lock reply.

Quirks:
	Our implementation gets very chatty in phase 3 of the protocol, since each Acceptor will send ACCEPTED messages to *all* of the nodes for *every* ACCEPT request it receives beyond n/2 (where n is the number of paxos nodes).
	
Outstanding issues:
	Node recovery is not implemented, nor is any form of paxos membership building/alteration. Failure of the node which has received a request before the accept requests have been sent out can cause the client to block permanently.
	
Run instructions:

	***** make a jar? compile from .java?
	We assign the clients to ports 9000 and 9001, whereas Paxos membership is hard coded as being the nodes which are listening to ports 9002 - 9006. 
