package sapphire.stats;

/**
 * Timer object for measuring runtimes. Creating a new StatTimer also starts the timer. 
 * Printing the timer will print the time since creation or restart.
 * If the timer is stopped, then it will print the time between start and stop.
 * 
 * @author iyzhang
 *
 */
public class Stopwatch {

	private long start;
	
	public Stopwatch() {
		start = System.nanoTime();
	}
	
	public Long stop() {
		long end = System.nanoTime();
		return Long.valueOf(end-start);
	}
	
	@Override
	public String toString() {
		long end = System.nanoTime();
		return Long.valueOf(end-start).toString();
	}
}
