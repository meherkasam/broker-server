import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.math.BigInteger;
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
	public static ConcurrentHashMap<String, DirectoryListObject> listOfFileObjects = new ConcurrentHashMap<String, DirectoryListObject>();
	public static ArrayList<ServerClientMapObject> serverClientMap;
	public static ArrayList<ListOfClientsObject> clientsList;
	public static ArrayList<ListOfServersObject> serversList;
	public String senderIP;
	public CommandProcessor() {		
		clientsList = new ArrayList<ListOfClientsObject>();
		serversList = new ArrayList<ListOfServersObject>();
	}	
	/**/
	public DataObject process(DataObject input) {
		String[] tokens = input.message.split(" ");
		if(tokens[0].compareToIgnoreCase("Req")==0) {
			if(tokens[1].compareToIgnoreCase("HELLO") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.isServer = input.isServer;
				a.senderId = input.senderId;
				Hello(a, tokens);
				System.out.println("Client says hello");
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("BYE") == 0) {
				DataObject a = new DataObject(0, -1);
				//Bye(a);
				System.out.println("Client says Bye");
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("LIST") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				//List(a, Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("GET") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				//Get(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUT") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				//Put(a, tokens[3], Integer.parseInt(tokens[4]));
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
		if(a.isServer) {
			//Add server to serverlist
			System.out.println("New server added: ");
			ListOfServersObject newServerObject = new ListOfServersObject(senderIP, Integer.parseInt(parameters[3]), Integer.parseInt(parameters[4]));
			serversList.add(newServerObject);
			System.out.println("Added to server list");
			int serverIndex = serversList.size() - 1;
			for(int i = 5; i < parameters.length; i++) {
				if(listOfFileObjects.get(parameters[i]) != null) {
					listOfFileObjects.get(parameters[i]).ListOfServers.add(serverIndex);
					System.out.println("File already exists " + parameters[i]);
				}
				else {
					DirectoryListObject newFileObject = new DirectoryListObject();
					newFileObject.ListOfServers.add(serverIndex);
					listOfFileObjects.put(parameters[i], newFileObject);
					System.out.println("New file added " + parameters[i]);
				}
			}
			//ListOfServersObject newServer = new ListOfServersObject(senderIP, port, a);
		}
		else {
			//Add client to clientlist
			int clientPort = Integer.parseInt(parameters[2]);
			ListOfClientsObject newClient = new ListOfClientsObject(senderIP, clientPort, a.senderId);
			clientsList.add(newClient);
			System.out.println("Added to client list");
		}
		int s = listOfFileObjects.size();
		System.out.println(s);
		a.message = "Rsp Hello " + String.valueOf(a.reqNo);
		return a;
	}
}
