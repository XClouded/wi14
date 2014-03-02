import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.HadroidMessage;
import message.RequestTaskMessage;
import message.ResultMessage;
import message.TaskMessage;

public class HadroidServer {

    private final int MAX_THREADS_COUNT = 10;
    // The server socket.
    private int serverPort;
    private HadroidJobsManager jobsManager;

    public HadroidServer(int port) {
        serverPort = port;
        jobsManager = new HadroidJobsManager();
    }

    public void start() {

        ServerSocket serverSocket = null;
        try {
            // Open a server socket on the port
            serverSocket = new ServerSocket(serverPort);
            //create a thread pool
            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS_COUNT);
            while (true) {
                    Socket connectionSocket = serverSocket.accept();
                    executor.execute(new ConnectionHandler(connectionSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
            
        } finally{
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public class ConnectionHandler implements Runnable{
        
        private Socket socket;
        
        public ConnectionHandler(Socket socket){
            this.socket = socket;
        }
        
        public void run(){
            try {
                InputStream in = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(in);
                try {
                    HadroidMessage msg = (HadroidMessage) ois.readObject();
                    HadroidMessage returnMsg = null;
                    if(msg instanceof RequestTaskMessage ){
                        returnMsg = new TaskMessage(jobsManager.getNextTask());
                    }else if (msg instanceof ResultMessage){
                        //
                        
                        //create return message
                        returnMsg = new TaskMessage(jobsManager.getNextTask());
                    }
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(returnMsg);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}
