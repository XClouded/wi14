import server.HadroidServer;


public class Main {
	
	private static final int portNumber = 6669;
	
	public static void main(String[] args){
		HadroidServer server = new HadroidServer(portNumber);
		server.start();
	}
}
