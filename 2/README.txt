Jake Sanders 0728906 jacobs22@u.washington.edu
Pingyang He 0929017 pingyh@uw.edu

Design:
	The state of all of the locks, the queue of pending lock requests, and the proposer, acceptor, and learner state machines for all instances of paxos are contained in the PaxosNodes. 
	
	The 'nodes' each occupy separate processes and communicate with each other via Java's datagram socket communication (UDP). This simplified the implementation, thanks to the lack of connections which created unnecessary complexity and possible deadlocks. This also eliminated some resiliance to finicky networks, but we decided to treat a dropped message in the same way as we treat any other kind of node failure. That is to say, with 5 nodes in the the group, Paxos can make progress even in the presence of 2 failures. This resiliance naturally goes up as the paxos membership increases.
	
	When a server receives any kind of message, it first determines whether it is part of the paxos protocol or associated with the lock service. In the former case, the message is processed by that round's Proposer, Acceptor, or Learner, depending of whether it is an prepare, promise, accept request, accept response, or a learn request. In the latter case, pre-processing is done to determine whether the request can be processed immediately, or whether it needs to be queued pending an unlock request. 
	
	Whenever a value is learned for a round, the node also notifies the requesting client at its designated port. The client blocks until a response is received pertaining to its current request. This eliminates the case where a response accidentally gets mistaken for a lock reply.
	
Run instructions:

	***** make a jar? compile from .java?
	We assign the clients to ports 9000 and 9001, whereas Paxos membership is hard coded as being the nodes which are listing to ports 9002 - 9006. 
