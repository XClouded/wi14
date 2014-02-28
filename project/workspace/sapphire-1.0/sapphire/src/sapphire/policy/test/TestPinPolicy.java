package sapphire.policy.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;

import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.policy.DefaultSapphirePolicy;
import sapphire.stats.Stopwatch;

public class TestPinPolicy extends DefaultSapphirePolicy {
	
	public static class TestPinServerPolicy extends DefaultServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public Long pin(InetSocketAddress address) {
			try {
				Stopwatch timer = new Stopwatch();
				sapphire_pin(address);
				return timer.stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static class TestPinGroupPolicy extends DefaultGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int NUM_TESTS = 1000;
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			TestPinServerPolicy testServer = (TestPinServerPolicy) server;
			try {
				ArrayList<InetSocketAddress> devices = sapphire_getDeviceServers();
				if (devices.isEmpty()) {
					super.onCreate(testServer);
					return;
				}
				BufferedWriter out1 = new BufferedWriter(new FileWriter("/bigraid/users/danava04/s_d.txt"));
				BufferedWriter out2 = new BufferedWriter(new FileWriter("/bigraid/users/danava04/d_d.txt"));
				BufferedWriter out3 = new BufferedWriter(new FileWriter("/bigraid/users/danava04/d_s.txt"));
				BufferedWriter out4 = new BufferedWriter(new FileWriter("/bigraid/users/danava04/s_s.txt"));
				
//				for (InetSocketAddress device : devices) {
//					System.out.println("device = " + device);
//				}
				// Assumes that this sapphire object was created on a server
				InetSocketAddress startServer = GlobalKernelReferences.nodeServer.getLocalHost();
				InetSocketAddress endServer = null;
				if (startServer.toString().contains("myers")) {
					endServer = new InetSocketAddress("moranis.cs.washington.edu", 44345);
				} else {
					endServer = new InetSocketAddress("myers.cs.washington.edu", 44345);
				}

				for (int i = 0; i < NUM_TESTS; ++i) {
					// pin from server to device
					//System.out.println("About to pin from server -> device");
					Long time1 = testServer.pin(devices.get(0));
					if (time1 == null) {
						throw new Exception("time is null");
					}
					//GlobalKernelReferences.stats.log(startServer + " -> " + devices.get(0), time);
					
					
					// pin from device to device
					//System.out.println("about to pin device -> device");
					Long time2 = testServer.pin(devices.get(1));
					if (time2 == null) {
						throw new Exception("time is null");
					}
					//GlobalKernelReferences.stats.log(devices.get(0) + " -> " + devices.get(1), time);
					
					
					// pin from device to server
					//System.out.println("about to pin device -> server");
					Long time3 = testServer.pin(endServer);
					if (time3 == null) {
						throw new Exception("time is null");
					}
					//GlobalKernelReferences.stats.log(devices.get(1) + " -> " + endServer, time);
					
					
					// pin from server to server
					//System.out.println("about to pin server -> server");
					Long time4 = testServer.pin(startServer);
					if (time4 == null) {
						throw new Exception("time is null");
					}
					//GlobalKernelReferences.stats.log(endServer + " -> " + startServer, time);
					
					
					//if (i >= 5000) {
						out1.write(time1 + "\n");
						out2.write(time2 + "\n");
						out3.write(time3 + "\n");
						out4.write(time4 + "\n");
					//}
				}
				out1.close();
				out2.close();
				out3.close();
				out4.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
