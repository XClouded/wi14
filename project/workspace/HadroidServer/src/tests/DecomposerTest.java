package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

public class DecomposerTest {

    @Test
    public void test() {
        String filePath = "db/WordCounter/tmp/hahaha.interm";
        File f = new File(filePath);
        try {
            f.createNewFile();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
            writer.println(4);
            writer.flush();
//            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
