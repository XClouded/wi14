package sapphire.appexamples.linpack.glue;

import com.example.linpack.R;

import sapphire.app.AndroidSapphireActivity;
import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.linpack.app.AndyMath;
import sapphire.appexamples.linpack.app.ImageRec;
import sapphire.appexamples.linpack.app.Linpack;
import sapphire.appexamples.linpack.app.Sudoku;
import sapphire.appexamples.linpack.app.WorldHarness;
import sapphire.appexamples.linpack.app.aichess4k;
import sapphire.appexamples.linpack.app.ocr.SampleData;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.res.AssetManager;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import sapphire.common.AppObjectStub;
import sapphire.oms.OMSServer;
import sapphire.runtime.SapphireActivityStarter;

public class MainActivity extends AndroidSapphireActivity {
    public static final String EXTRA_MESSAGE = "";

    public static class thing implements Serializable{
		private static final long serialVersionUID = 1L;
		String method;
		ArrayList<Object> params;
	}
    
    public static class Return implements Serializable {
    	private static final long serialVersionUID = 1L;
    	public double mflops;
        public double timems;
        public double residn;
        public double precision;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		long duration = 0;
		try {
			ByteArrayOutputStream btemp = new ByteArrayOutputStream();
			ObjectOutputStream outtemp = new ObjectOutputStream(btemp);
			outtemp.writeObject(new Return());
			for(int i = 0; i < 1100; i++) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(b);
				thing rpc = new thing();
				
				ByteArrayInputStream c = new ByteArrayInputStream(btemp.toByteArray());
				ObjectInputStream in = new ObjectInputStream(c);
				rpc.method = "sapphire.appexamples.linpack.app.run_benchmark";
				rpc.params = new ArrayList<Object>();

				long start = System.nanoTime();
		        Return t = (Return) in.readObject();
		        in.close();
				out.writeObject(rpc);
				out.close();
				if(i > 100)
					duration += System.nanoTime() - start;
			}
		} catch(Exception ex) {}
		Log.v("","" + duration/1000);
		//if(this != null)
		//	return;
		
		final Linpack l = (Linpack)DoCall.doNow(this,Action.getAppEntry);
		Log.v("Linpack", "got entrypoint " + l);
		final AndyMath m = (AndyMath)DoCall.doNow(l,Action.getAndyMath);
		Log.v("Linpack", "AndyMath " + m);
		final Sudoku s = (Sudoku)DoCall.doNow(l,Action.getSudoku);
		Log.v("Linpack", "Sudoku " + s);
		final ImageRec i = (ImageRec)DoCall.doNow(l,Action.getImageRec);
		Log.v("Linpack", "ImageRec " + i);
		final WorldHarness w = (WorldHarness)DoCall.doNow(l,Action.getPhysics);
		Log.v("Linpack", "World " + w);
		final aichess4k c = (aichess4k)DoCall.doNow(l,Action.getChessAI);
		Log.v("Linpack", "Chess " + c);
		
		final TextView text = (TextView) findViewById(R.id.textView1);
		
		final Button linpack = (Button) findViewById(R.id.button1);
		linpack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				List<Long> store = new ArrayList<Long>();
				for(int i = 0; i < n; i++) {
					long start = System.nanoTime();
					DoCall.doNow(l,Action.run_benchmark);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("Linpack", out);
				text.setText("Linpack/n"+out);
			}
		});
		
		final Button calculus = (Button) findViewById(R.id.button2);
		calculus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				List<Long> store = new ArrayList<Long>();
				for(int i = 0; i < n; i++) {
					long start = System.nanoTime();
					//function, variable, start, stop
					DoCall.doNow(m,Action.integrate,"sin(x)","x",""+0.0,""+1.0);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("Calculus", out);
				text.setText("Calculus/n"+out);
			}
		});
		
		final Button sudoku = (Button) findViewById(R.id.button3);
		sudoku.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				List<Long> store = new ArrayList<Long>();
				for(int i = 0; i < n; i++) {
					int[][] matrix = new int[9][9];
					matrix[0][0] = 5;
					matrix[0][2] = 9;
					
					matrix[1][0] = 7;
					matrix[1][4] = 5;
					matrix[1][6] = 2;
					
					matrix[2][1] = 6;
					matrix[2][3] = 1;
					
					matrix[3][2] = 6;
					matrix[3][5] = 7;
					matrix[3][7] = 3;
					
					matrix[4][2] = 1;
					matrix[4][3] = 5;
					matrix[4][5] = 9;
					matrix[4][6] = 6;
					
					matrix[5][1] = 8;
					matrix[5][3] = 4;
					matrix[5][6] = 1;
					
					matrix[6][5] = 8;
					matrix[6][7] = 7;
					
					matrix[7][2] = 8;
					matrix[7][4] = 4;
					matrix[7][8] = 1;
					
					matrix[8][6] = 3;
					matrix[8][8] = 2;
					long start = System.nanoTime();
					DoCall.doNow(s,Action.solve,matrix);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("Sudoku", out);
				text.setText("Sudoku/n" +out);
			}
		});
		
		final Button imagerec = (Button) findViewById(R.id.button4);
		imagerec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				String file = null;
				try {
					AssetManager assetManager = getAssets();
					InputStream asset;
					asset = assetManager.open("sample.dat");
					file = new java.util.Scanner(asset).useDelimiter("\\A").next();
				} catch (IOException e) {e.printStackTrace();}
				DoCall.doNow(i,Action.init,file);
				List<SampleData> in = new ArrayList<SampleData>();
				for(int k = 0; k < 50; k++) {
					SampleData input = new SampleData((char) 0, 5, 7);
					for(int i = 0; i < 5; i++)
						for(int j = 0; j < 7; j++)
							input.setData(i, j, true);
					in.add(input);
				}
				List<Long> store = new ArrayList<Long>();
				for(int j = 0; j < n; j++) {
					long start = System.nanoTime();
					DoCall.doNow(i,Action.main,in);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("ImageRec", out);
				text.setText("ImageRec/n" + out);
			}
		});
		
		final Button physics = (Button) findViewById(R.id.button5);
		physics.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				DoCall.doNow(w,Action.setup);
				List<Long> store = new ArrayList<Long>();
				for(int j = 0; j < n; j++) {
					long start = System.nanoTime();
					DoCall.doNow(w,Action.step);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("World", out);
				text.setText("World/n" + out);
			}
		});
		
		final Button chess = (Button) findViewById(R.id.button6);
		chess.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int n = 1100;
				List<Long> store = new ArrayList<Long>();
				for(int j = 0; j < n; j++) {
					if(n%20 == 0)
						DoCall.doNow(c,Action.reset);
					long start = System.nanoTime();
					DoCall.doNow(c,Action.runAI);
					if(n >= 100)
						store.add(System.nanoTime() - start);
				}
				String out = interpret(store);
				Log.v("Chess", out);
				text.setText("Chess/n" + out);
			}
		});
	}
	
	
	private static String interpret(List<Long> store) {
		int n = store.size();
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
		return "" + sum + " ns\n" + "dev: " + devsum + " ns\n max: " + max +" ns";
	}
	
    public enum Action {
    	//linpack
    	run_benchmark, run, getAndyMath, getSudoku, getImageRec, getPhysics, getChessAI,
    	//andymath
    	integrate,
    	//sudoku
    	solve,
    	//ImageRec
    	main, init,
    	//Physics
    	step, setup,
    	//ChessAI
    	runAI, reset,
    	//general
    	getAppEntry
    }
    
    public static class DoCall extends AsyncTask<Object, Void, Object> {
    	
    	public static Object doNow(Object... args) {
    		try {
				return new DoCall().execute(args).get();
			} catch (Exception e) {
				Log.v("Linpack", "had exception: " + e);
				return null;
			}
    	}
    	
    	public static DoCall start(Object... args) {
    		try {
				DoCall out = new DoCall();
				out.execute(args);
				return out;
			} catch (Exception e) {
				//TODO: shouldn't do this
				Log.v("Linpack", "had exception: " + e);
				return null;
			}
    	}

		@Override
		protected Object doInBackground(Object... args) {
			switch((Action)args[1]) {//determine method
				case run_benchmark:
					return ((Linpack)args[0]).run_benchmark();
				case run:
					return ((Linpack)args[0]).run((Integer)args[2]);
				case getAndyMath:
					return ((Linpack)args[0]).getAndyMath();
				case getSudoku:
					return ((Linpack)args[0]).getSudoku();
				case getImageRec:
					return ((Linpack)args[0]).getImageRec();
				case getPhysics:
					return ((Linpack)args[0]).getPhysics();
				case getChessAI:
					return ((Linpack)args[0]).getChessAI();
					
				case integrate:
					return ((AndyMath)args[0]).integrate((String)args[2],(String)args[3],(String)args[4],(String)args[5]);
					
				case solve:
					return ((Sudoku)args[0]).solve((int[][])args[2]);
					
				case main:
					return ((ImageRec)args[0]).main((List<SampleData>) args[2]);
				case init:
					return ((ImageRec)args[0]).init((String)args[2]);
					
				case step:
					return ((WorldHarness)args[0]).step();
				case setup:
					return ((WorldHarness)args[0]).init();
					
				case runAI:
					return ((aichess4k)args[0]).runAI();
				case reset:
					((aichess4k)args[0]).init();
					return null;
					
				case getAppEntry: return ((MainActivity)args[0]).getAppEntryPoint();
			}
			return null;
		}
    	
    }
}
