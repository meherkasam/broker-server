import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

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
	public static ConcurrentHashMap<Socket, ConnectionObject> listOfConnections = new ConcurrentHashMap<Socket, ConnectionObject>();
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
	    			brokerserver.log.println(input.message);
	    			brokerserver.log.flush();
	    		}
	    		catch(Exception E){
	    	    	//System.out.println("Client disconnected");
	    			if(processor.isServer) {	
	    				System.out.println("Server " + processor.currentId + " crashed");
	    				processor.serverCrashHandler();	    				
	    			}
	    			else {	
	    				System.out.println("Client " + processor.currentId + " crashed");
	    				processor.clientCrashHandler();
	    			}
	    			clientSocket.close();
	    			break;
	    		}
	    		output = processor.process(input);
	    		brokerserver.log.println(output.message);
	    		brokerserver.log.flush();
	    		os.writeObject(output);
    			if(output.message.compareToIgnoreCase("Rsp Bye")==0) {
    				clientSocket.close();
    				break;
    			}
	    	}
		}
		catch(IOException E){
	    	System.out.println("Client " + processor.currentId + " crashed");
			processor.clientCrashHandler();
		}
		catch(Exception e){
			System.out.println("Client " + processor.currentId + " crashed");
			processor.clientCrashHandler();
		}
	}
}
