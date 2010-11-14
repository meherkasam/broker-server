import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 
 * @author meher
 *
 * This class makes handling of multiple clients possible
 * through threading. Each thread reads objects directly
 * from the network stream and calls the processor function of
 * the CommandProcessor class and writes the returned objects
 * directly to the network stream.
 * 
 */

public class MultiListen implements Runnable{
	Socket clientSocket = null;
	DataObject input = null, output = null;
	public MultiListen(Socket a){
		clientSocket = a;
	}
	public void run() {
		ObjectInputStream is = null;
		ObjectOutputStream os = null;
		CommandProcessor processor = new CommandProcessor();
		processor.senderIP = clientSocket.getInetAddress().getHostAddress();
		try {
			is = new ObjectInputStream(clientSocket.getInputStream());
	    	os = new ObjectOutputStream(clientSocket.getOutputStream());
	    	while (true) {
	    		try {
	    			input = (DataObject) is.readObject();
	    		}
	    		catch (Exception E) {
	    			System.out.println(E);
	    		}
	    		output = processor.process(input);
	    		os.writeObject(output);
    			if(output.message.compareToIgnoreCase("Rsp Bye")==0){
    				clientSocket.close();
    				break;
    			}
	    	}
		}
		catch(IOException E){
	    	System.out.println("Client disconnected");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
}
