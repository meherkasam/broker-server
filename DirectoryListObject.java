import java.io.File;
import java.util.*;

public class DirectoryListObject {
	
	public CustomReadWriteLock lock;
	public ArrayList<Integer> ListOfServers;
	DirectoryListObject() {
		lock = new CustomReadWriteLock();
		ListOfServers = new ArrayList<Integer>();
	}
}
