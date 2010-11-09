import java.io.File;
import java.util.*;

public class DirectoryListObject {
	
	public File fileHandle;
	public String fileName;
	public CustomReadWriteLock lock;
	public ArrayList<Integer> ListOfServers;
	DirectoryListObject(String fname, File handle) {
		fileName = fname;
		fileHandle = handle;
		lock = new CustomReadWriteLock();
		ListOfServers = new ArrayList<Integer>();
	}
}
