import java.util.ArrayList;

public class brokerserver {

	public ArrayList<ServerClientMapObject> serverclientmap;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Listener();
		new CommandProcessor();
	}

}