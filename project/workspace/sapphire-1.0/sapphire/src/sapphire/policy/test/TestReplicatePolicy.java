package sapphire.policy.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import sapphire.policy.DefaultSapphirePolicy;
import sapphire.stats.Stopwatch;

public class TestReplicatePolicy extends DefaultSapphirePolicy {
	
	public static class TestReplicateServerPolicy extends DefaultServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public Long replicate() {
			Stopwatch timer = new Stopwatch();
			sapphire_replicate();
			return timer.stop();
		}
	}
	
	public static class TestReplicateGroupPolicy extends DefaultGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		static final int TEST_NUM = 1000;
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			for (int i = 0; i < TEST_NUM; ++i) {
				Long time = ((TestReplicateServerPolicy)server).replicate();
				System.out.println("replicate time = " + time);
			}
		}
	}

}
