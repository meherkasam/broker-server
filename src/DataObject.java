import java.io.Serializable;

public class DataObject implements Serializable {
	public String message;
	public byte[] data;
	public int length;
	public int reqNo;
	boolean success = true;
	static final long serialVersionUID = 42L;
	public String senderId;
	public boolean isServer;
	public DataObject(int size) {
		size*=1024;
		message = "";
		if(size == 0) {
			length = 0;
			data = null;
		}
		else {
			data = new byte[size];
		}
	}
	public DataObject(int size, int reqNum){
		reqNo = reqNum;
		message = "";
		if(size == 0) {
			length = 0;
			data = null;
		}
		else {
			data = new byte[size];
		}
	}
}
