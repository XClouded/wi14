package sapphire.appexamples.microbenchmark.device;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.microbenchmark.app.TestObject;
import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.stats.Stopwatch;

public class MicrobenchmarkTestActivity implements SapphireActivity {
	
	private TestObject test;
	
	public class TestThread extends Thread {
		private int numPings;
		private String testString;
		
		public TestThread(int numPings, String testString) {
			this.numPings = numPings;
			this.testString = testString;
		}
		
		public void run() {
			try {
				Stopwatch timer = new Stopwatch();
				test.ping();
				GlobalKernelReferences.stats.log("MicrobenchmarkFirstRPC", timer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < numPings; i++) {
				try {
					Stopwatch timer = new Stopwatch();
					test.ping();
					GlobalKernelReferences.stats.log(testString, timer);
				}  catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void runThreadedTest(int numThreads, int numPings) {
	       TestThread threads[] = new TestThread[numThreads];

	       for (int i = 0; i < numThreads; i++) {
	    	   threads[i] = new TestThread(numPings, "MicrobenchmarkRPC-"+Integer.valueOf(numThreads).toString());
	    	   threads[i].start();
	       }
	        
	       for (int i = 0; i < numThreads; i++) {
	    	   TestThread t = threads[i];
	        	try {
	        		t.join();
	        	} catch (InterruptedException e) {
	        		System.out.println("Interrupted thread. Ignoring.");
	        		continue;
	        	}
	        }
	       
	}
	
	public void onCreate(SapphireObject appEntryPoint) {
		try {			
			test = (TestObject) appEntryPoint;
            System.out.println("Received Test object: " + test);
        } catch (Exception e) {
			e.printStackTrace();
		}
		runThreadedTest(1, 1000);
		System.out.println("Test Summary Threads: 1");
		System.out.print("+");
		GlobalKernelReferences.stats.printSummary("MicrobenchmarkFirstRPC");
		System.out.print("+");
		GlobalKernelReferences.stats.printSummary("MicrobenchmarkRPC-1");
		System.out.println("***********************************************");

		for (int n = 10; n <= 100; n+= 10) {
			runThreadedTest(n, 1000);
			
			System.out.println("Test Summary Threads: "+Integer.valueOf(n));
			System.out.print("+");
			GlobalKernelReferences.stats.printSummary("MicrobenchmarkFirstRPC");
			System.out.print("+");
			GlobalKernelReferences.stats.printSummary("MicrobenchmarkRPC-"+Integer.valueOf(n).toString());
			System.out.println("***********************************************");
		}
		for (int n = 200; n <= 1000; n+= 100) {
			runThreadedTest(n, 1000);
			
			System.out.println("Test Summary Threads: "+Integer.valueOf(n));
			System.out.print("+");
			GlobalKernelReferences.stats.printSummary("MicrobenchmarkFirstRPC");
			System.out.print("+");
			GlobalKernelReferences.stats.printSummary("MicrobenchmarkRPC-"+Integer.valueOf(n).toString());
			System.out.println("***********************************************");
		}

 	}
}
