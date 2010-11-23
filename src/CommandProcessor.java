import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.util.Iterator;

/**
 * 
 * @author meher
 *
 * This class acts as a processor of the various commands
 * issued by the client. It processes the input, calls
 * the relevant functions which complete the task and control
 * is returned to the MultiListen class.
 * 
 */

public class CommandProcessor {
	public final static int KB2B = 1024;
	static String serverRoot = "./";
	FileOutputStream streamToBeWritten = null;
	int currentId;
	boolean isServer = false;
	public static ConcurrentHashMap<String, DirectoryListObject> listOfFileObjects = new ConcurrentHashMap<String, DirectoryListObject>();
	public static CopyOnWriteArrayList<ServerClientMapObject> serverClientMap = new CopyOnWriteArrayList<ServerClientMapObject>();
	public static CopyOnWriteArrayList<ListOfClientsObject> clientsList = new CopyOnWriteArrayList<ListOfClientsObject>();
	public static CopyOnWriteArrayList<ListOfServersObject> serversList = new CopyOnWriteArrayList<ListOfServersObject>();
	public boolean currentlyWriting = false;
	String currFileName = "";
	//public Socket currentSocket = null;
	public String senderIP;
	int currentPriority = 1;
	public CommandProcessor() {		
		//currentSocket = mySock;
	}	
	public DataObject process(DataObject input) {
		String[] tokens = input.message.split(" ");
		if(tokens[0].compareToIgnoreCase("Req")==0) {
			if(tokens[1].compareToIgnoreCase("HELLO") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.isServer = input.isServer;
				a.senderId = input.senderId;
				Hello(a, tokens);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("BYE") == 0) {
				DataObject a = new DataObject(0, -1);
				Bye(a);
				System.out.println("Bye message received");
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("LIST") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				List(a, Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("GETDONE") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[3]));
				a.senderId = input.senderId;
				GetDone(a, tokens[2]);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("GET") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Get(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUTDONE") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[3]));
				a.senderId = input.senderId;
				PutDone(a, tokens[2]);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUT") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Put(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("DELETE") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Delete(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUSH") == 0) {
				DataObject a = new DataObject(Integer.parseInt(tokens[6]), Integer.parseInt(tokens[2]));
				a.data = input.data;
				a.length = input.length;
				a.senderId = input.senderId;
				boolean isLast = false;
				if(tokens[4].compareTo("LAST") == 0) {
					isLast = true;
				}
				Push(a, tokens[3], Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]), isLast);
				return a;
			}
			else {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.message = "RSP 0x003";
				return a;
			}
		}
		return null;
	}
	DataObject Hello(DataObject a, String[] parameters) {
		//MultiListen.listOfConnections.get(currentSocket).isServer = a.isServer;
		isServer = a.isServer;
		if(a.isServer) {
			//Add server to serverlist
			System.out.println("Server says hello");
			ListOfServersObject newServerObject = new ListOfServersObject(senderIP, Integer.parseInt(parameters[3]), Integer.parseInt(parameters[4]));
			serversList.add(newServerObject);
			String servers = "";
			for(int z = 0; z < serversList.size(); z++) {
				if(serversList.get(z).status)
					servers += serversList.get(z).ServerIP.toString() + " " + serversList.get(z).ServerComPort + " ";
			}
			System.out.println("Servers list: " + servers);
			int serverIndex = serversList.size() - 1;
			currentId = serverIndex;
			//MultiListen.listOfConnections.get(currentSocket).index = serverIndex;
			for(int i = 5; i < parameters.length; i++) {
				if(listOfFileObjects.get(parameters[i]) != null) {
					listOfFileObjects.get(parameters[i]).listOfServers.add(serverIndex);
					System.out.println("File already exists " + parameters[i]);
				}
				else {
					DirectoryListObject newFileObject = new DirectoryListObject(parameters[i]);
					newFileObject.listOfServers.add(serverIndex);
					listOfFileObjects.put(parameters[i], newFileObject);
					System.out.println("New file added " + parameters[i]);
				}
			}
		}
		else {
			//Add client to clientlist
			int clientPort = Integer.parseInt(parameters[2]);
			ListOfClientsObject newClient = new ListOfClientsObject(senderIP, clientPort, a.senderId);
			currentId = clientsList.size();
			//MultiListen.listOfConnections.get(currentSocket).index = currentClientId;
			clientsList.add(newClient);
			System.out.println("Client says hello");
		}
		a.message = "Rsp Hello " + String.valueOf(a.reqNo);
		return a;
	}
	DataObject List(DataObject a, int start, int max, int priority) {
		String fileList = "";
		int j = 0;
		a.message = "Rsp List " + String.valueOf(a.reqNo);
	    Iterator<DirectoryListObject> ii = listOfFileObjects.values().iterator();
		while(ii.hasNext() && j < max) {
			DirectoryListObject currFile = (DirectoryListObject) ii.next();
			String fileName = currFile.fileName;
			fileList += " " + fileName;
			j++;
		}
	    a.message += " " + Integer.toString(start) + " " + Integer.toString(j) + fileList;
	    return a;
	}
	DataObject Bye(DataObject a) {
		a.message = "Rsp Bye";
		return a;
	}
	DataObject GetDone(DataObject a, String fName) {
		int currentConnection = 0;
		a.message = "Rsp Getdone SUCCESS";
		for(int i = 0; i < serverClientMap.size(); i++) {
			if(serverClientMap.get(i).ClientIndex == currentId) {
				currentConnection = i;
				break;
			}
		}
		String fileName = serverClientMap.get(currentConnection).fileName;
		listOfFileObjects.get(fileName).lock.readerDone();
		serversList.get(serverClientMap.get(currentConnection).ServerIndex).CurrentLoad--;
		serverClientMap.remove(currentConnection);
		return a;
	}
	DataObject Get(DataObject a, String fileName, int priority) {
		boolean foundServer = false, error = false;
		int minLoadServer = 0, minLoadIndex = 0;
		a.message = "Rsp Get " + String.valueOf(a.reqNo);
		if(listOfFileObjects.containsKey(fileName)) {
			DirectoryListObject targetFile = listOfFileObjects.get(fileName);
			minLoadIndex = targetFile.listOfServers.get(minLoadServer);
			for(int i = 0; i < targetFile.listOfServers.size(); i++) {
				if((serversList.get(targetFile.listOfServers.get(i)).CurrentLoad < serversList.get(targetFile.listOfServers.get(i)).MaxLoad) && serversList.get(targetFile.listOfServers.get(i)).status) {
					minLoadIndex = i;
					foundServer = true;
				}
			}
		}
		if(!foundServer) {
			a.message += " " + fileName + " FAILURE 0x005";
			a.success = false;
		}
		else {
			try{
				DirectoryListObject x = listOfFileObjects.get(fileName);
				x.lock.getReadLock(priority);
			}
			catch (Exception E){
				a.message += " " + fileName + " FAILURE 0x005";
				error = true;
				a.success = false;
			}
			if(!error) {
				a.message += " " + fileName + " READY " + serversList.get(minLoadIndex).ServerIP.getHostAddress() + " " + serversList.get(minLoadIndex).ServerComPort;
				ServerClientMapObject connection = new ServerClientMapObject(minLoadIndex, currentId, fileName);
				serversList.get(minLoadIndex).CurrentLoad++;
				serverClientMap.add(connection);
				
			}
		}
		return a;
	}
	DataObject PutDone(DataObject a, String fName) {
		int currentConnection = 0;
		//a.message = "Rsp Putdone SUCCESS";
		for(int i = 0; i < serverClientMap.size(); i++) {
			if(serverClientMap.get(i).ClientIndex == currentId) {
				currentConnection = i;
				break;
			}
		}
		String fileName = serverClientMap.get(currentConnection).fileName;
		listOfFileObjects.get(fileName).lock.writerDone();
		serverClientMap.remove(currentConnection);
		return a;
	}
	DataObject Put(DataObject a, String fileName, int priority) {
		currentPriority = priority;
		boolean foundServer = false;
		currentlyWriting = true;
		currFileName = fileName;
		int minLoadServer = 0;
		a.message = "Rsp Put " + String.valueOf(a.reqNo);
		for(int i = 0; i < serversList.size(); i++) {
			if(serversList.get(i).CurrentLoad < serversList.get(i).MaxLoad) {
				if(serversList.get(i).status) {
					foundServer = true;
					minLoadServer = i;
					break;
				}
			}
		}
		if(!foundServer) {
			a.message += " " + fileName + " FAILURE 0x005";
			a.success = false;
			return a;
		}
		a.message += " " + fileName + " READY " + serversList.get(minLoadServer).ServerIP.getHostAddress() + " " + serversList.get(minLoadServer).ServerComPort;
		if(!listOfFileObjects.containsKey(fileName))
			listOfFileObjects.put(fileName, new DirectoryListObject(fileName));
		//listOfFileObjects.get(fileName).listOfServers.add(minLoadServer);
		listOfFileObjects.get(fileName).lock.getWriteLock(priority);
		//ServerClientMapObject connection = new ServerClientMapObject(minLoadServer, currentId, fileName);
		//serverClientMap.add(connection);
		return a;
	}
	DataObject Push(DataObject a, String fileName, int startByte, int length, boolean isLast) {
		a.message = "Rsp Push " + String.valueOf(a.reqNo);
		System.out.println("Push requested: " + fileName);
		File fileToBeWritten = new File(Listener.tmpRoot + "tmp_" + fileName);
		try {
			if(streamToBeWritten == null)
				streamToBeWritten = new FileOutputStream(fileToBeWritten);
			streamToBeWritten.write(a.data, 0, length);
			streamToBeWritten.flush();
			if (isLast) {
				streamToBeWritten.close();
				streamToBeWritten = null;
				listOfFileObjects.get(fileName).lock.writerDone();
				fileToBeWritten.renameTo(new File(fileName));
				currentlyWriting = false;
				currFileName = "";
				FSPutter myputter = new FSPutter(fileName, currentPriority, true);
				Thread t = new Thread(myputter);
				t.start();
			}
		}
		catch(IOException e) {
			a.message += " " + "FAILURE 0x005";
			a.success = false;
			return a;
		}
		a.message += " " + "SUCCESS " + Integer.toString(length);
		return a;
	}
	DataObject Delete(DataObject a, String fileName, int priority) {
		boolean error = !listOfFileObjects.containsKey(fileName);
		a.message = "Rsp Delete " + String.valueOf(a.reqNo) + " " + fileName;
		if(!error) {
			FSPutter myputter = new FSPutter(fileName, priority, false);
			Thread t = new Thread(myputter);
			t.start();
			/*listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			deleted = toBeDeleted.delete();
			listOfFileObjects.get(fileName).lock.writerDone();
			listOfFileObjects.remove(fileName);*/
			a.message += " SUCCESS";
		}
		else {
			a.success = false;
			a.message += " FAILURE 0x005";
		}
		return a;
	}
	void serverCrashHandler() {
		Iterator<ServerClientMapObject> ii = serverClientMap.iterator();
		while(ii.hasNext()) {
			ServerClientMapObject currConnection = (ServerClientMapObject) ii.next();
			if(currConnection.ServerIndex == currentId) {
				System.out.println ("Connection closed: " + currConnection.ServerIndex + " " + currConnection.ClientIndex);
				ii.remove();
			}
		}
		serversList.get(currentId).status = false;
		Iterator<DirectoryListObject> jj = listOfFileObjects.values().iterator();
		while(jj.hasNext()) {
			DirectoryListObject currFile = jj.next();
			for(int i = 0; i < currFile.listOfServers.size(); i++) {
				if(currFile.listOfServers.get(i) == currentId) {
					currFile.listOfServers.remove(i);
					break;
				}
			}
			if(currFile.listOfServers.size() == 0) {
				jj.remove();
			}
		}
	}
	void clientCrashHandler() {
		if(currentlyWriting) {
			File toBeDeleted = new File(Listener.tmpRoot + "tmp_" + currFileName);
			System.out.println("File deleted");
			streamToBeWritten = null;
			currentlyWriting = false;
			currFileName = "";
			try {
				toBeDeleted.delete();
			}
			catch(Exception E) {
				System.out.println("Error deleting");
			}
		}
		Iterator<ServerClientMapObject> ii = serverClientMap.iterator();
		while(ii.hasNext()) {
			ServerClientMapObject currConnection = (ServerClientMapObject) ii.next();
			if(currConnection.ClientIndex == currentId) {
				System.out.println ("Connection closed: " + currConnection.ServerIndex + " " + currConnection.ClientIndex);
				ii.remove();
				break;
			}
		}
		clientsList.get(currentId).status = false;
	}
}
