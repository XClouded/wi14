package sapphire.kernel.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Semaphore;

import sapphire.common.ObjectHandler;
import sapphire.kernel.common.KernelObjectMigratingException;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;
import sapphire.stats.Stopwatch;

/** 
 * A single Sapphire kernel object that can receive RPCs.
 * These are stored in the Sapphire kernel server. 
 * @author iyzhang
 *
 */

public class KernelObject extends ObjectHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_CONCURRENT_RPCS = 10000;
	private Boolean coalesced;
	private Semaphore rpcCounter;
	
	// For high latency notifications
	private long maxLatency;
	private SapphireServerPolicy serverPolicy;
	
	public KernelObject(Object obj) {
		super(obj);
		coalesced = false;
		rpcCounter = new Semaphore(MAX_CONCURRENT_RPCS, true);
		maxLatency = -1;
		serverPolicy = null;
	}
	
	public Object invoke(String method, Object[] params) throws Exception {
		Object ret;
		
		if (coalesced) {
			throw new KernelObjectMigratingException();
		}
		
		rpcCounter.acquire();
		//Stopwatch timer = new Stopwatch();
		ret = super.invoke(method, params);
		//long latency = timer.stop();
		rpcCounter.release();
		
		//if (maxLatency >= 0 && latency > maxLatency)
		//	serverPolicy.onHighLatency();
		
		return ret;
	}
	
	public void coalesce() {
		coalesced = true;
		while (rpcCounter.availablePermits() < MAX_CONCURRENT_RPCS - 1) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				continue;
			}
		}
		
		return;
	}
	
	public void uncoalesce() {
		coalesced = false;
		// reset the rpc semaphore
		rpcCounter = new Semaphore(MAX_CONCURRENT_RPCS, true);
	}
	
	/**
	 * Registers this kernel object to notify the server policy if the RPC latency is greater
	 * than the given max latency.
	 * @param maxLatency
	 * @param serverPolicy
	 */
	public void registerLatencyCallback(long maxLatency, SapphireServerPolicy serverPolicy) {
		this.maxLatency = maxLatency;
		this.serverPolicy = serverPolicy;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(super.getObject());
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		Object obj = in.readObject();
		super.fillMethodTable(obj);
		super.setObject((Serializable)obj);
	}
}