package server;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import message.HadroidMessage;
import message.PingAliveMessage;
import message.RequestTaskMessage;
import message.ResultMessage;
import message.TaskMessage;
import task.HadroidTask;

public class HadroidServer {

    private final int MAX_THREADS_COUNT = 10;
    // The server socket.
    private int serverPort;
    private HadroidJobsManager jobsManager;
    private Logger logger;

    public HadroidServer(int port) {
        serverPort = port;
        jobsManager = new HadroidJobsManager();
        logger = Logger.getLogger(this.getClass().getName());
        
        //for testing purpose. TODO remove this later
        jobsManager.addHadroidJob("WordCounter");
        
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
                HadroidMessage msg = (HadroidMessage) ois.readObject();
                System.out.println("request received");
                HadroidMessage returnMsg = null;
                if(msg instanceof RequestTaskMessage ){
                    HadroidTask t = jobsManager.getNextTask();
                    if(t == null){
                        //no task available
                    }else{
                        returnMsg = new TaskMessage(t);
                    }
                }else if (msg instanceof ResultMessage){
                    //
                    jobsManager.taskIsDone((ResultMessage)msg);
                    
                    //create return message
                    returnMsg = new TaskMessage(jobsManager.getNextTask());
                }else if(msg instanceof PingAliveMessage){
                    UUID clientID = ((PingAliveMessage) msg).getClientID();
                    
                }
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(returnMsg);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
