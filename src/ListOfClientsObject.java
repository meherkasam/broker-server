public class ListOfClientsObject {
	public String ClientIP; //Used if client on a different machine than localhost
	public int ClientComPort; //Used to identify the port in the client to be used for data communication
	public String LastRequest; //Used to identify the last request made (e.g. get, put)
	public String clientId;
	
	ListOfClientsObject( String cip, int cport, String cid ){
		ClientIP = cip;
		ClientComPort = cport;
		clientId = cid;
	}
	public void setLastRequest(String lastreq){
		LastRequest = lastreq;
	}
	public String getLastRequest(){
		return LastRequest;
	}
}