import java.util.Comparator;
public class RequestQueueComparator implements Comparator<RequestQueueObject>{
	public int compare(RequestQueueObject x, RequestQueueObject y) {
		if(x.getPriority() < y.getPriority())
			return -1;
		else if (x.getPriority() > y.getPriority())
			return 1;
		return 0;
	}
}
