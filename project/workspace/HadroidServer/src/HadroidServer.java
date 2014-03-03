import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.HadroidMessage;
import message.RequestTaskMessage;
import message.ResultMessage;
import message.TaskMessage;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;

public class HadroidServer {

    private final int MAX_THREADS_COUNT = 10;
    // The server socket.
    private int serverPort;
    private HadroidJobsManager jobsManager;

    public HadroidServer(int port) {
        serverPort = port;
        jobsManager = new HadroidJobsManager();
        
        fillJobManager();
        
    }
    
    private void fillJobManager() {
        Class cls = loadClass();
        HadroidMapReduceJob job;
        try {
            job = (HadroidMapReduceJob) cls
                    .getDeclaredConstructor(String.class)
                    .newInstance("/Users/isphrazy/Documents/study/CSE/550/" +
                            "hw/wi14/project/workspace/data/sh.txt");
            jobsManager.addHadroidJob(job);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Class loadClass(){
        File file1 = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/bin/");
//        File file2 = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/bin/wordcounter/WordCounter$WordCounterReduce.class");
//        File file3 = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/bin/wordcounter/WordCounter$WordCounterMap.class");
        try {
            System.out.println(file1.exists());
            System.out.println(file1.toURI().toURL());
//            System.out.println(file1.exists() && file2.exists() && file3.exists());
//            URL[] urls = new URL[]{file1.toURI().toURL(), file2.toURI().toURL(), file3.toURI().toURL()};
            URL[] urls = {file1.toURI().toURL()};

            ClassLoader parentClassLoader = this.getClass().getClassLoader();
            // Create a new class loader with the directory
            URLClassLoader cl = new URLClassLoader(urls);
            return cl.loadClass("WordCounter");
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
        return null;
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
                    returnMsg = new TaskMessage(jobsManager.getNextTask());
                }else if (msg instanceof ResultMessage){
                    //
                    
                    //create return message
                    returnMsg = new TaskMessage(jobsManager.getNextTask());
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
