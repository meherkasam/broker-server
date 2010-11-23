import java.io.*;
import java.lang.Thread;
import java.security.*;
import javax.net.ssl.*;

public class Listener {
	static String tmpRoot = "tmp/";
	SSLServerSocket listenerSocket = null;
	SSLSocket clientSocket = null;
	boolean ssldebug = false;
	public Listener(String params[]) {
		int port = 6000;
		if(params.length > 0) {
			if(params[0].compareTo("ssldebug") == 0) {
				ssldebug = true;
			}
		}
		String ksName = "herong.jks";
		char ksPass[] = "HerongJKS".toCharArray();
		char ctPass[] = "HerongJKS".toCharArray();
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName), ksPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			listenerSocket = (SSLServerSocket) ssf.createServerSocket(port);
			listenerSocket.setEnabledCipherSuites(listenerSocket.getSupportedCipherSuites());
	    }
	    catch (IOException e) {
	    	System.out.println("Unable to create listener socket: " + e);
	    	System.exit(1);
	    }
	    catch (Exception e) {
	         System.err.println(e.toString());
	    }
	    System.out.println ("Created listener socket successfully on port no. "+ port);
	    while(true){
	    	try{
		    	clientSocket = (SSLSocket) listenerSocket.accept();
		    	clientSocket.startHandshake();
		    	if(ssldebug)
		    		printSocketInfo(clientSocket);
				System.out.println("Machine connected");
				MultiListen myListener = new MultiListen(clientSocket);
				Thread t = new Thread(myListener);
				t.start();
		    }
		    catch(IOException e){
		    	System.out.println(e);
		    }
		    catch (Exception e) {
		         System.err.println(e.toString());
		    }
	    }
	}
	private static void printSocketInfo(SSLSocket s) {
	      System.out.println("Socket class: "+s.getClass());
	      System.out.println("   Remote address = "
	         +s.getInetAddress().toString());
	      System.out.println("   Remote port = "+s.getPort());
	      System.out.println("   Local socket address = "
	         +s.getLocalSocketAddress().toString());
	      System.out.println("   Local address = "
	         +s.getLocalAddress().toString());
	      System.out.println("   Local port = "+s.getLocalPort());
	      System.out.println("   Need client authentication = "
	         +s.getNeedClientAuth());
	      SSLSession ss = s.getSession();
	      System.out.println("   Cipher suite = "+ss.getCipherSuite());
	      System.out.println("   Protocol = "+ss.getProtocol());
	   }
}
