
public class ServerClientMapObject {
	public int ServerIndex;
	public int ClientIndex;
	public boolean ServerState = true;
	ServerClientMapObject(int sindex, int cindex){
		ServerIndex = sindex;
		ClientIndex = cindex;
	}
	public void setServerState(boolean state){
		ServerState = state;
	}
	public boolean getServerState(){
		return ServerState;
	}	
}
