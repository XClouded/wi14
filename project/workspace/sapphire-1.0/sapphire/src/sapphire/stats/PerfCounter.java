package sapphire.stats;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Keeps a list of samples and gives the 
 * @author iyzhang
 *
 */
public class PerfCounter {
	private ArrayList<Long> stats;
	private String name;
	
	public PerfCounter(String name) {
		stats = new ArrayList<Long>();
		this.name = name;
	}
	
	public synchronized void push(Long stat) {
		stats.add(stat);
	}
	
	public synchronized void push(ArrayList<Long> stats) {
		this.stats.addAll(stats);
	}
	
	public synchronized Long average() {
		long total = 0;
		for (Iterator<Long> it = stats.iterator(); it.hasNext(); ) {
			total += it.next();
		}
		return total/stats.size();
	}
	
	public synchronized Long max() {
		long max = Long.MIN_VALUE;
		for (Iterator<Long> it = stats.iterator(); it.hasNext(); ) {
			long next = it.next();
			if (next > max) {
				max = next;
			}
		}
		return max;
	}
	
	public synchronized Long min() {
		long min = Long.MAX_VALUE;
		for (Iterator<Long> it = stats.iterator(); it.hasNext(); ) {
			long next = it.next();
			if (next < min) {
				min = next;
			}
		}
		return min;
	}
	
	public synchronized void clear() {
		stats.clear();
	}
	
	public synchronized String toString() {
		return name+"\t"+average()+"\t"+min()+"\t"+max();
	}

}
