public class ListOfClientsObject {
	public String ClientIP; //Used if client on a different machine than localhost
	public String ClientComPort; //Used to identify the port in the client to be used for data communication
	public String LastRequest; //Used to identify the last request made (e.g. get, put)
	
	ListOfClientsObject( String cip, String cport, String lastreq ){
		ClientIP = cip;
		ClientComPort = cport;
		LastRequest = lastreq;
	}
	public void setLastRequest(String lastreq){
		LastRequest = lastreq;
	}
	public String getLastRequest(){
		return LastRequest;
	}	
}