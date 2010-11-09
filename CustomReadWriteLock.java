import java.util.concurrent.PriorityBlockingQueue;

/**
 * 
 * @author meher
 *
 * Each object of this type accompanies an object of
 * FileObject type. Any thread that would want to 
 * perform any operation on any file would have to
 * obtain and release locks on this object.
 *
 */

class CustomReadWriteLock {

	private int totalReadLocksGiven;
	private boolean writeLockIssued;
	private int threadsWaitingForWriteLock;
	RequestQueueComparator comparator;
	PriorityBlockingQueue<RequestQueueObject> readerRequestQueue;
	PriorityBlockingQueue<RequestQueueObject> writerRequestQueue;
	
	public CustomReadWriteLock() {
		writeLockIssued = false;
		comparator = new RequestQueueComparator();
		readerRequestQueue = new PriorityBlockingQueue<RequestQueueObject>();
		writerRequestQueue = new PriorityBlockingQueue<RequestQueueObject>();
	}
	
	public void getReadLock(int priority) {
		RequestQueueObject waitObject = new RequestQueueObject(priority);
		synchronized (waitObject) {
			RequestQueueObject highestPriorityWriter = writerRequestQueue.peek();
			int highestPriority = -1;
			if(highestPriorityWriter != null)
				highestPriority = highestPriorityWriter.getPriority();
			while ((writeLockIssued) || ( highestPriority > priority)) {
				try {
					readerRequestQueue.add(waitObject);
					System.out.println("Reader waiting");
					waitObject.wait();
					System.out.println("Reader released");
					highestPriorityWriter = writerRequestQueue.peek();
					highestPriority = -1;
					if(highestPriorityWriter != null)
						highestPriority = highestPriorityWriter.getPriority();
				}
				catch (InterruptedException e) {
					
				}
				catch (Exception e) {
						
				}
			}
			totalReadLocksGiven++;
			System.out.println("Read lock issued");
		}
	}

	public void getWriteLock(int priority) {
		RequestQueueObject waitObject = new RequestQueueObject(priority);
		threadsWaitingForWriteLock++;
		while ((totalReadLocksGiven != 0) || (writeLockIssued)) {
			try {
				writerRequestQueue.add(waitObject);
				synchronized (waitObject) {
					System.out.println("Writer waiting");
					waitObject.wait();
					System.out.println("Writer released");
				}
			}
			catch (InterruptedException e) {
				
			}
		}
		threadsWaitingForWriteLock--;
		System.out.println("Write lock issued");
		writeLockIssued = true;
	}

	public void readerDone() {
		totalReadLocksGiven--;
		if (totalReadLocksGiven == 0) {
			if(threadsWaitingForWriteLock > 0) {
				RequestQueueObject nextWriter = writerRequestQueue.remove();
				synchronized (nextWriter) {
					nextWriter.notify();
				}
			}			
		}
	}
	
	public void writerDone() {
		writeLockIssued = false;
		int highestReaderPriority = -1;
		int highestWriterPriority = -1;
		if(!readerRequestQueue.isEmpty()) {
			highestReaderPriority = readerRequestQueue.peek().getPriority();
		}
		if(!writerRequestQueue.isEmpty()) {
			highestWriterPriority = writerRequestQueue.peek().getPriority();
		}
		if (highestReaderPriority == -1 && highestWriterPriority == -1)
			return;
		if (highestWriterPriority >= highestReaderPriority) {
			RequestQueueObject nextWriter = writerRequestQueue.remove();
			synchronized (nextWriter) {
				nextWriter.notify();
			}
		}
		else {
			while(highestReaderPriority > highestWriterPriority) {
				RequestQueueObject nextReader = readerRequestQueue.remove();
				synchronized (nextReader) {
					nextReader.notify();
				}
				if(readerRequestQueue.isEmpty())
					break;
				highestReaderPriority = readerRequestQueue.peek().getPriority();
			}
		}		
	}
}