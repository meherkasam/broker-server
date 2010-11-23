import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryListObject {
	public String fileName;
	public CustomReadWriteLock lock;
	public CopyOnWriteArrayList<Integer> listOfServers;
	DirectoryListObject(String fName) {
		fileName = fName;
		lock = new CustomReadWriteLock();
		listOfServers = new CopyOnWriteArrayList<Integer>();
	}
}
