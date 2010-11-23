import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


public class brokerserver {
	static PrintStream log;
	public static void main(String[] args) {
		try {
			log = new PrintStream(new File("operations.log"));
		} catch (FileNotFoundException e) {
			System.out.println("Log file not found");
		}
		new Listener(args);
		new CommandProcessor();
	}

}