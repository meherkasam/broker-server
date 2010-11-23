import java.util.*;

public class DirectoryListObject {
	public String fileName;
	public CustomReadWriteLock lock;
	public ArrayList<Integer> listOfServers;
	DirectoryListObject(String fName) {
		fileName = fName;
		lock = new CustomReadWriteLock();
		listOfServers = new ArrayList<Integer>();
	}
}
