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
	public static ConcurrentHashMap<String, DirectoryListObject> listOfFileObjects = null;
	public static ArrayList<ServerClientMapObject> serverClientMap;
	public static ArrayList<ListOfClientsObject> clientsList;
	public static ArrayList<ListOfServersObject> serversList;
	public String clientIP;
	public CommandProcessor() {		
		clientsList = new ArrayList<ListOfClientsObject>();
	}	
	/**/
	public DataObject process(DataObject input) {
		String[] tokens = input.message.split(" ");
		if(tokens[0].compareToIgnoreCase("Req")==0) {
			if(tokens[1].compareToIgnoreCase("HELLO") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[3]));
				a.clientId = input.clientId;
				int clientPort = Integer.parseInt(tokens[2]);
				Hello(a, clientPort);
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
				a.clientId = input.clientId;
				//Get(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUT") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.clientId = input.clientId;
				//Put(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("DELETE") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.clientId = input.clientId;
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
	DataObject Hello(DataObject a, int port) {
		if(a.isServer) {
			//Add server to serverlist
		}
		else {
			//Add client to clientlist
			ListOfClientsObject newClient = new ListOfClientsObject(clientIP, port, a.clientId);
			clientsList.add(newClient);
		}
		a.message = "Rsp Hello " + String.valueOf(a.reqNo);
		return a;
	}
}
