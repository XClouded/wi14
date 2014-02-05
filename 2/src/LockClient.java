import java.io.BufferedReader;
import java.io.InputStreamReader;


public class LockClient {
	private static final String EXIT = "exit";
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String command = null;
		System.out.println("Client started!");
		while(!EXIT.equals(command.toLowerCase())) {
			// TODO command receive loop
		}
	}
}
