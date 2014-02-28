package sapphire.appexamples.linpack.app;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.clustercopy.SOMClusterCopyTraining;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import sapphire.app.SapphireObject;
import sapphire.appexamples.linpack.app.ocr.SampleData;
import sapphire.policy.oahu.OahuPolicy;

public class ImageRec implements SapphireObject<OahuPolicy> {

	private static final long serialVersionUID = 1L;
	static final int DOWNSAMPLE_WIDTH = 5;
	static final int DOWNSAMPLE_HEIGHT = 7;
	static final String homeDir = "/scratch/ackeri/out/apps/Linpack/bin/classes/";
	private SOM net;
	private SampleData sample;
	private ArrayList<SampleData> letterListModel = new ArrayList<SampleData>();
	
    public String main(List<SampleData> dss) {
    	String out = "";
    	long start = System.nanoTime();
    	for(SampleData ds : dss) {
	
			final MLData input = new BasicMLData(5*7);
			int idx = 0;
			for (int y = 0; y < ds.getHeight(); y++) {
				for (int x = 0; x < ds.getWidth(); x++) {
					input.setData(idx++, ds.getData(x, y) ? .5 : -.5);
				}
			}

			final int best = this.net.classify(input);
			final char map[] = mapNeurons();
			out += ""+map[best];
    	}
		System.out.println("Execution time:	" + (System.nanoTime() - start));
		return out;
    }
    
	char[] mapNeurons() {
		final char map[] = new char[this.letterListModel.size()];

		for (int i = 0; i < map.length; i++) {
			map[i] = '?';
		}
		for (int i = 0; i < this.letterListModel.size(); i++) {
			final MLData input = new BasicMLData(DOWNSAMPLE_WIDTH * DOWNSAMPLE_HEIGHT);
			int idx = 0;
			final SampleData ds = (SampleData) this.letterListModel.get(i);
			for (int y = 0; y < ds.getHeight(); y++) {
				for (int x = 0; x < ds.getWidth(); x++) {
					input.setData(idx++, ds.getData(x, y) ? .5 : -.5);
				}
			}

			final int best = this.net.classify(input);
			map[best] = ds.getLetter();
		}
		return map;
	}
    
    public int init(String in) {
    	/*try {
    		System.out.println("Opening File");
			FileReader f;// the actual file stream
			BufferedReader r;// used to read the file line by line

			f = new FileReader(new File(homeDir + "sample.dat"));
			r = new BufferedReader(f);
			String line;
			int i = 0;

			this.letterListModel.clear();

			while ((line = r.readLine()) != null) {
				final SampleData ds = new SampleData(line.charAt(0),
						DOWNSAMPLE_WIDTH, DOWNSAMPLE_HEIGHT);
				this.letterListModel.add(i++, ds);
				int idx = 2;
				for (int y = 0; y < ds.getHeight(); y++) {
					for (int x = 0; x < ds.getWidth(); x++) {
						ds.setData(x, y, line.charAt(idx++) == '1');
					}
				}
			}

			r.close();
			f.close();
			
		} catch (final Exception e) {
			e.printStackTrace();
		}*/
    	String[] lines = in.split("\n");
    	for(int i = 0; i < lines.length; i++) {
    		final SampleData ds = new SampleData(lines[i].charAt(0),
					DOWNSAMPLE_WIDTH, DOWNSAMPLE_HEIGHT);
			this.letterListModel.add(ds);
			int idx = 2;
			for (int y = 0; y < ds.getHeight(); y++) {
				for (int x = 0; x < ds.getWidth(); x++) {
					ds.setData(x, y, lines[i].charAt(idx++) == '1');
				}
			}
    	}
    	try {
    		System.out.println("Training");
			final int inputNeuron = DOWNSAMPLE_HEIGHT * DOWNSAMPLE_WIDTH;
			final int outputNeuron = this.letterListModel.size();

			final MLDataSet trainingSet = new BasicMLDataSet();
			for (int t = 0; t < this.letterListModel.size(); t++) {
				final MLData item = new BasicMLData(inputNeuron);
				int idx = 0;
				final SampleData ds = (SampleData) this.letterListModel.get(t);
				for (int y = 0; y < ds.getHeight(); y++) {
					for (int x = 0; x < ds.getWidth(); x++) {
						item.setData(idx++, ds.getData(x, y) ? .5 : -.5);
					}
				}

				trainingSet.add(new BasicMLDataPair(item, null));
			}

			this.net = new SOM(inputNeuron,outputNeuron);
			this.net.reset();

			SOMClusterCopyTraining train = new SOMClusterCopyTraining(this.net,trainingSet);
			
			train.iteration();
		} catch (final Exception e) {
			e.printStackTrace();
		}

    	return 0;
    }

    
}
