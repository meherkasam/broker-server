import java.io.*;
import java.net.Inet4Address;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.CopyOnWriteArrayList;

public class FSPutter implements Runnable {
	public static int reqNo = 0;
	String fileName;
	public final static int KB2B = 1024;
	public static int chunkSize = 1000;
	public static String senderId = "0000";
	public ObjectInputStream is = null;
	public ObjectOutputStream os = null;
	static PrintStream log = null;
	boolean isPut = false;
	int priority = 1;
	FSPutter(String fName, int p, boolean putOperation) {
		isPut = putOperation;
		priority = p;
		fileName = fName;
		try {
			log = new PrintStream(new FileOutputStream("fsput.log"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public void run() {
		if(isPut) {
			boolean fileFound = false;
			if(CommandProcessor.listOfFileObjects.get(fileName).listOfServers.size() > 0)
				fileFound = true;
			int leastLoad = 10000;
			int minLoadServer = -1;
			CommandProcessor.listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			if(!fileFound) {
				for(int i = 0; i < CommandProcessor.serversList.size(); i++) {
					if((CommandProcessor.serversList.get(i).CurrentLoad < leastLoad) && CommandProcessor.serversList.get(i).status) {
						minLoadServer = i;
						leastLoad = CommandProcessor.serversList.get(i).CurrentLoad;
					}
				}
				if(minLoadServer != -1) {
					Inet4Address fileServerAddress = CommandProcessor.serversList.get(minLoadServer).ServerIP;
					int fileServerPort = CommandProcessor.serversList.get(minLoadServer).ServerComPort;
					CommandProcessor.serversList.get(minLoadServer).CurrentLoad++;
					Execute(fileServerAddress, fileServerPort);
					CommandProcessor.serversList.get(minLoadServer).CurrentLoad--;
					CommandProcessor.listOfFileObjects.get(fileName).listOfServers.add(minLoadServer);
				}
			}
			else {
				CopyOnWriteArrayList<Integer> myList = CommandProcessor.listOfFileObjects.get(fileName).listOfServers;
				for(int i = 0; i < myList.size(); i++) {
					ListOfServersObject myServer = CommandProcessor.serversList.get(myList.get(i));
					myServer.CurrentLoad++;
					Execute(myServer.ServerIP, myServer.ServerComPort);
					myServer.CurrentLoad--;
				}
			}
			CommandProcessor.listOfFileObjects.get(fileName).lock.writerDone();
		}
		else {
			CommandProcessor.listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			CopyOnWriteArrayList<Integer> myList = CommandProcessor.listOfFileObjects.get(fileName).listOfServers;
			for(int i = 0; i < myList.size(); i++) {
				ListOfServersObject myServer = CommandProcessor.serversList.get(myList.get(i));
				ExecuteDelete(myServer.ServerIP, myServer.ServerComPort);
			}
			CommandProcessor.listOfFileObjects.get(fileName).lock.writerDone();
			CommandProcessor.listOfFileObjects.remove(fileName);
		}
	}
	void Execute(Inet4Address serverAddress, int serverPort) {
		DataObject input, output;
		input = new DataObject(0);
		output = new DataObject(0);
		try {
			SSLSocketFactory g = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket fileServerSocket = (SSLSocket) g.createSocket(serverAddress, serverPort);
			fileServerSocket.setEnabledCipherSuites(fileServerSocket.getSupportedCipherSuites());
			os = new ObjectOutputStream(fileServerSocket.getOutputStream());
			is = new ObjectInputStream(fileServerSocket.getInputStream());
			input = Put(output, fileName, 1000, 0);
    		if(input.success) {
    			//String message = input.message;
    			input = new DataObject(0);
    			output = new DataObject(chunkSize);
    			input = Push(output, fileName, 0);
    			if (input.success)
    				System.out.println("Put file " + fileName + " successful\n");
    			else
    				System.out.println("Error putting " + fileName + "\n");
    		}
    		else {
    			System.out.println("Error putting " + fileName + "\n");
    		}
    		os.close();
    		is.close();
		}
		catch (Exception e) {
			System.out.println("Connection issues");
		}
	}
	void ExecuteDelete(Inet4Address serverAddress, int serverPort) {
		DataObject input, output;
		input = new DataObject(0);
		output = new DataObject(0);
		try {
			SSLSocketFactory g = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket fileServerSocket = (SSLSocket) g.createSocket(serverAddress, serverPort);
			fileServerSocket.setEnabledCipherSuites(fileServerSocket.getSupportedCipherSuites());
			os = new ObjectOutputStream(fileServerSocket.getOutputStream());
			is = new ObjectInputStream(fileServerSocket.getInputStream());
			input = Delete(output, fileName, priority);
    		if(input.success) {
    			System.out.println("Delete file " + fileName + " successful\n");
    		}
    		else {
    			System.out.println("Error deleting " + fileName + "\n");
    		}
    		os.close();
    		is.close();
		}
		catch (Exception e) {
			System.out.println("Connection issues");
		}
	}
	DataObject Put(DataObject a, String fileName, int priority, int limit) {
		reqNo++;
		a.reqNo = reqNo;
		a.senderId = senderId;
		a.message = "Req Put " + String.valueOf(a.reqNo) + " " + fileName + " " + priority;
		Write(a);
		a = Read(a);
		return a;
	}
	DataObject Push(DataObject a, String fileName, int limit) {
		a.reqNo = reqNo;
		int offset = 0;
		boolean notLast = true;
		int currentChunk = 0;
		try {
			FileInputStream is = new FileInputStream(fileName);
			while (notLast) {
				currentChunk++;
				a.senderId = senderId;
				a.message = "Req Push " + String.valueOf(a.reqNo) + " " + fileName;
				int length = is.read(a.data, 0, chunkSize * KB2B);
				a.length = length;
				if (length < chunkSize * KB2B || currentChunk == limit) {
					notLast = false;
				}
				if (notLast) {
					a.message += " NOTLAST";
				} else {
					a.message += " LAST";
				}
				a.message += " " + (offset) + " " + length;
				if (notLast) {
					offset += a.length;
				}
				Write(a);
				a = Read(a);
			}
			is.close();
		} catch (IOException e) {

		}
		return a;
	}
	DataObject Delete(DataObject a, String fileName, int priority) {
		reqNo++;
		a.reqNo = reqNo;
		a.senderId = senderId;
		a.message = "Req Delete " + String.valueOf(a.reqNo) + " " + fileName
				+ " " + priority;
		Write(a);
		a = Read(a);
		return a;
	}
	DataObject Read(DataObject input) {
		try {
			input = (DataObject) is.readObject();
			log.println(input.message);
		} catch (Exception E) {

		}
		return input;
	}

	void Write(DataObject output) {
		try {
			os.writeObject(output);
			os.flush();
			log.println(output.message);
			log.flush();
		} catch (IOException e) {
			System.out.println("Server Crash Write1");
		} catch (Exception E) {
			System.out.println("Server Crash Write2");
		}
	}
}
