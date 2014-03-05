package tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.Test;

import server.HadroidJobsManager;
import task.HadroidTask;

public class JobsManagerTest {

    @Test
    public void testNoEmptyLine() {
        HadroidJobsManager jm = new HadroidJobsManager();
        jm.addHadroidJob("WordCounter");
        HadroidTask t1 = null;
        int i = 0;
        while ((t1 = jm.getNextTask()) != null) {

            List<String> lines = t1.getData();
            for (String line : lines) {
                assertTrue(line.trim().length() > 0);
            }
            // break;
            System.out.println(lines.get(lines.size() - 1));
            System.out.println("----------------");
            // FileOutputStream fos = new FileOutputStream("" + i +
            // "_output.txt");
        }
    }

}
