
public class ServerClientMapObject {
	public int ServerIndex;
	public int ClientIndex;
	public String fileName;
	public boolean ServerState = true;
	ServerClientMapObject(int sindex, int cindex, String fName){
		ServerIndex = sindex;
		ClientIndex = cindex;
		fileName = fName;
	}
	public void setServerState(boolean state){
		ServerState = state;
	}
	public boolean getServerState(){
		return ServerState;
	}	
}
