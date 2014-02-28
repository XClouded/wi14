package sapphire.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class StatsManager {

	private class StatsManagerThread extends Thread {
		public void run() {
			while (true) {
				if (!stats.isEmpty()) {
					System.out.println(new Date(System.currentTimeMillis()));
					System.out.println("Perf Stat Tag\tAverage\tMin\tMax");
					
					ArrayList<PerfCounter> statCounters;
					
					synchronized (stats) {
						statCounters = new ArrayList<PerfCounter>(stats.values());
					}
					
					for (Iterator<PerfCounter> it = statCounters.iterator(); it.hasNext();) {
						System.err.println(it.next());
					}
					System.out.println("-----------------------------------------------");
				}
				// sleep for 1 second
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	StatsManagerThread thread;
	HashMap<String,PerfCounter> stats;
	
	public StatsManager() {
		stats = new HashMap<String,PerfCounter>();
		thread = new StatsManagerThread();
		thread.start();
	}
	
	private void log(String name, Stopwatch timer, Boolean logToFile) {
		Long time = timer.stop();
		PerfCounter counter;
		synchronized (stats) {
			if (stats.containsKey(name)) {
				counter = stats.get(name);
			} else {
				if (logToFile) {
					counter = new LoggedPerfCounter(name);
				} else {
					counter = new PerfCounter(name);
				}
				stats.put(name, counter);
			}
		}
		counter.push(time);
	}
	
	public void log(String name, Stopwatch timer) {
		log(name, timer, false);
	}
	
	public void logToStdout(String name, Stopwatch timer) {
		System.out.println("["+name+"] "+timer.stop());
	}
	
	public void logToFile(String name, Stopwatch timer) {
		log(name, timer, true);
	}
	
	public void printSummary(String name) {
		System.out.println(stats.get(name));
	}
}
