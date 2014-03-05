package edu.uw.HadroidServerTester;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

import task.HadroidTask;

import message.HadroidMessage;
import message.RequestTaskMessage;
import message.TaskMessage;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
    
//    private final String SERVER_IP = "172.28.7.96";
    private final String SERVER_IP = "10.0.2.2";
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
        
        for(int i = 0; i < 1; i++){
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
                ObjectOutputStream oos = new ObjectOutputStream(outstream);
                oos.writeObject(new RequestTaskMessage());
                
                InputStream in = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(in);
                Log.d("HADROID_TESTER", "received: ");
                try {
                    HadroidMessage msg = (HadroidMessage) ois.readObject();
                    if(msg instanceof TaskMessage){
                        HadroidTask task = ((TaskMessage) msg).getTask();
                        if(task == null) {
                            Log.d("HADROID_TESTER", "nullllllllllll");
                        }
                        task.getFunction().run(task.getData());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
