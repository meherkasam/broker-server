import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
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
	private static ConcurrentHashMap<String, File> lookedUpFiles = null;
	static String serverRoot = "./";
	FileOutputStream streamToBeWritten = null;
	int currentId;
	boolean isServer = false;
	public static ConcurrentHashMap<String, DirectoryListObject> listOfFileObjects = new ConcurrentHashMap<String, DirectoryListObject>();
	public static ArrayList<ServerClientMapObject> serverClientMap = new ArrayList<ServerClientMapObject>();
	public static ArrayList<ListOfClientsObject> clientsList = new ArrayList<ListOfClientsObject>();
	public static ArrayList<ListOfServersObject> serversList = new ArrayList<ListOfServersObject>();
	//public Socket currentSocket = null;
	public String senderIP;
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
				//Delete(a, tokens[3], Integer.parseInt(tokens[4]));
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
			if(serversList.get(minLoadIndex).CurrentLoad < serversList.get(minLoadIndex).MaxLoad) {
				foundServer = true;
			}
			for(int i = 1; i < targetFile.listOfServers.size(); i++) {
				if((serversList.get(targetFile.listOfServers.get(i)).CurrentLoad < serversList.get(targetFile.listOfServers.get(minLoadIndex)).CurrentLoad) && (serversList.get(targetFile.listOfServers.get(i)).CurrentLoad < serversList.get(targetFile.listOfServers.get(i)).MaxLoad)) {
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
		a.message = "Rsp Putdone SUCCESS";
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
		boolean fileFound = false, foundServer = false;
		int minLoadServer = 0;
		a.message = "Rsp Put " + String.valueOf(a.reqNo);
		if(listOfFileObjects.containsKey(fileName)) {
			fileFound = true;
		}
		if(fileFound) {
			a.message += " " + fileName + " FAILURE 0x005";
			a.success = false;
			return a;
		}
		else {
			for(int i = 0; i < serversList.size(); i++) {
				if(serversList.get(i).CurrentLoad < serversList.get(i).MaxLoad) {
					foundServer = true;
					minLoadServer = i;
					break;
				}
			}
			if(!foundServer) {
				a.message += " " + fileName + " FAILURE 0x005";
				a.success = false;
				return a;
			}
			a.message += " " + fileName + " READY " + serversList.get(minLoadServer).ServerIP.getHostAddress() + " " + serversList.get(minLoadServer).ServerComPort;
			listOfFileObjects.put(fileName, new DirectoryListObject(fileName));
			listOfFileObjects.get(fileName).listOfServers.add(minLoadServer);
			listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			ServerClientMapObject connection = new ServerClientMapObject(minLoadServer, currentId, fileName);
			serverClientMap.add(connection);
		}
		return a;
	}
}
