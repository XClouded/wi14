package edu.uw.HadroidServerTester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
    
    private final String SERVER_IP = "192.168.0.20";
    private final int SERVER_PORT = 6669;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button bt = (Button)findViewById(R.id.send_btn);
//        bt.addOn
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void sendToServer(View view) {
        
        for(int i = 0; i < 10; i++){
            Talker t = new Talker();
            t.execute(i);
        }
    }
    
    class Talker extends AsyncTask<Integer, Void, Void> {
        
        @Override
        protected Void doInBackground(Integer... arg0) {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                socket.setTcpNoDelay(true);
                OutputStream outstream = socket.getOutputStream(); 
//                DataOutputStream dos = new DataOutputStream(outstream);
//                dos.write(("Hello from " + arg0[0]).getBytes());
//                dos.flush();
                PrintWriter output = new PrintWriter(outstream, true);
                ObjectOutputStream oos = new ObjectOutputStream(outstream);
                oos.writeObject("Hello from " + arg0[0]);
                
                InputStream in = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(in);
                try {
                    String s = (String) ois.readObject();
                    System.out.println("received: " + s);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                outstream.write(("Hello from " + arg0[0]).getBytes());
//                outstream.flush();
//                BufferedWriter out = new BufferedWriter(
//                        new OutputStreamWriter(socket.getOutputStream()));
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(socket.getInputStream()));
////                
//////              mStatusText.setText("Sending Data to PC");    
//                Log.d("HADROID_TESTER", "sending");
//                output.println("Hello from " + arg0); 
//                output.flush();
//                output.checkError();
//                outstream.flush();
//                socket.setTcpNoDelay(true);
//                String msg = "Hello from " + arg0[0];
//                out.write(msg.getBytes());
//                Log.d("HADROID_TESTER", "sending...");
//                out.flush();
//                out.close();
//                output.close();
                
                
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                byte buffer[] = new byte[1024];
//                for(int s; (s=in.read(buffer)) != -1; )
//                {
//                    baos.write(buffer, 0, s);
//                }
//                Log.d("HADROID_TESTER", "received: " + baos.toString());
//                Log.d("HADROID_TESTER", "received: " + in.readLine());
//                out.flush();
//              mStatusText.setText("Data sent to PC");            

//                socket.close();                                    
//              mStatusText.setText("Socket closed");
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  

            return null;
        }
    }
    
    public abstract class Message implements Serializable{
       public MsgType msgType;
       
       
    }
    
    public class RequestTaskMsg extends Message{
        
        public RequestTaskMsg(){
            msgType = MsgType.REQUEST_TASK;
        }
    }
    
    public class TaskMsg extends Message{
        
    }
    
    public class ResultMsg extends Message{
        
        
    }
    
    public enum MsgType{
        REQUEST_TASK,
        TASK,
        RESULT,
    }
    
}
