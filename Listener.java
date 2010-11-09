import java.io.*;
import java.net.*;
import java.lang.Thread;

public class Listener{
	
	ServerSocket listenerSocket = null;
	Socket clientSocket = null;
	public Listener(){
		
		int port=6000;
	    try{
	    		listenerSocket = new ServerSocket(port);
	    }
	    catch (IOException e){
	    	System.out.println("Unable to create listener socket: " + e);
	    	System.exit(1);
	    }
	    System.out.println ("Created listener socket successfully on port no. "+ port);
	    while(true){
	    	try{
		    	clientSocket = listenerSocket.accept();
		    	System.out.println("Client connected");
		    	MultiListen myListener = new MultiListen(clientSocket);
		    	Thread t = new Thread(myListener);
		    	t.start();
		    }
		    catch(IOException e){
		    	System.out.println(e);
		    }	    	
	    }
	}
	
}
