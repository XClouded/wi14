package sapphire.appexamples.linpack.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.util.Log;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.linpack.app.AndyMath;
import sapphire.appexamples.linpack.app.ImageRec;
import sapphire.appexamples.linpack.app.Linpack;
import sapphire.appexamples.linpack.app.WorldHarness;
import sapphire.appexamples.linpack.app.ocr.SampleData;
import sapphire.appexamples.linpack.glue.MainActivity.Action;
import sapphire.appexamples.linpack.glue.MainActivity.DoCall;
import sapphire.common.AppObject;
import sapphire.kernel.server.KernelServer;
import sapphire.kernel.server.KernelServerImpl;
import sapphire.oms.OMSServer;


public class BenchmarkActivity implements SapphireActivity {

	@Override
	public void onCreate(SapphireObject arg0) {
		Linpack l = (Linpack)arg0;
		System.out.println("Linpack" + l);
		AndyMath m = l.getAndyMath();
		WorldHarness w = l.getPhysics();
		ImageRec j = l.getImageRec();
		//j.init();
		SampleData input = new SampleData((char) 0, 5, 7);
		for(int i = 0; i < 5; i++)
			for(int k = 0; k < 7; k++)
				input.setData(i, k, true);
		
		int n = 1000;
		String out = "";
		List<Long> store = new ArrayList<Long>();
		for(int i = 0; i < n; i++) {
			long start = System.nanoTime();
			//out = DoCall.doNow(l,Action.run_benchmark) + "\nTotal latency: " + (System.nanoTime() - start)/1000000.0 + " ms";
			//l.run_benchmark();
			//m.integrate("x^2","x",""+0.0,""+1.0);
			store.add(System.nanoTime() - start);
		}
		long sum = 0;
		long max = -1;
		for(int i = 0; i < store.size(); i++) {
			sum += store.get(i);
			if(store.get(i) > max)
				max = store.get(i);
		}
		sum /= n;
		
		long devsum = 0;
		for(int i = 0; i < store.size(); i++) {
			long temp = sum - store.get(i);
			temp *= temp;
			devsum += temp;
		}
		devsum /= n-1;
		devsum = (long) Math.sqrt(devsum);
		out = "" + sum + "ms\n" + "dev: " + devsum + "\n max: " + max;
		System.out.println(out);
	}
}




