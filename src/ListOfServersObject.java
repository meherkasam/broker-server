import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ListOfServersObject {
	public Inet4Address ServerIP; //Used if server on a different machine than localhost
	public int ServerComPort; //Used to identify the port in the server to be used for data communication 
	//public int ServerPollPort;//Used to identify the port in the server to be used for polling for liveness
	public int CurrentLoad; //To store the current load of the server
	public int MaxLoad; //To store the current load of the server
	public boolean status;
	ListOfServersObject( String sip, int sport, int maxload ){
		status = true;
		try {
			ServerIP = (Inet4Address) Inet4Address.getByName(sip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerComPort = sport;
		CurrentLoad = 0;
		MaxLoad = maxload;
	}
	public void setServerLoad(int load){
		CurrentLoad = load;
	}
	public int getServerLoad(){
		return CurrentLoad;
	}	
}
