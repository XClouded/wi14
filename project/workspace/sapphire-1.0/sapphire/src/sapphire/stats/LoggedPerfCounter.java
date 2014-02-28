package sapphire.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class LoggedPerfCounter extends PerfCounter {

	private static String statsDir = "/bigraid/users/"+System.getProperty("user.name")+"/sapphire/logs/";
	private BufferedWriter out;
	private File file;
	private long samples = 0;
	private long total = 0;
	private long max = Long.MIN_VALUE;
	private long min = Long.MAX_VALUE;
	
	public LoggedPerfCounter(File file) {
		super(file.getName());
		this.file = file;
		try {
			out = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LoggedPerfCounter() {
		super("unamed sapphire counter");
		try {
			file = File.createTempFile("sapphire", ".stats", new File(statsDir));
			out = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LoggedPerfCounter(String name) {
		super(name);
		String filename = statsDir + name + ".stats";
		
		file = new File(filename);
		try {
			file.createNewFile();
			out = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public synchronized void push(Long stat) {
		samples++;
		total += stat.longValue();
		if (stat > max) {
			max = stat.longValue();
		}
		if (stat < min) {
			min = stat.longValue();
		}
		try {
			out.write(stat.toString());
			out.newLine();
		} catch (IOException e) {
			System.err.println("Stats file not available: "+file.getName());
		}
	}
	
	public void push(ArrayList<Long> stats) {
		for (Iterator<Long> it = stats.iterator(); it.hasNext(); ) {
			push(it.next());			
		}
	}
	
	public synchronized void flush() {
		try {
			out.flush();
		} catch (IOException e) {
			System.err.println("Stats file not available: "+file.getName());
		}
	}
	
	public Long average() {
		return Long.valueOf(total/samples);
	}
	
	public Long min() {
		return Long.valueOf(min);
	}
	
	public Long max() {
		return Long.valueOf(max);
	}
	
	public void clear() {
		
	}
}
