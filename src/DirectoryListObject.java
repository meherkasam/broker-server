import java.io.File;
import java.util.*;

public class DirectoryListObject {
	public File fileHandle;
	public CustomReadWriteLock lock;
	public ArrayList<Integer> listOfServers;
	DirectoryListObject(String fileName) {
		fileHandle = new File(fileName);
		lock = new CustomReadWriteLock();
		listOfServers = new ArrayList<Integer>();
	}
}
