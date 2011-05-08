package challenge;

// needs to be synchronized, since can be accessed from more then a single doGet/doPost() that run in parallel
public class UploadDescriptor implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		UNKNOWN, DOING, DONE, ERROR
	};

	public static final UploadDescriptor UNKNOWN; // kinda NULL/notfound descriptor
	static {
		UNKNOWN = new UploadDescriptor();
		UNKNOWN.status = Status.UNKNOWN;
	}

	private String absolutePath;
	private int bytesSoFar;
	private int totalBytes;
	private int partsSoFar;
	private int totalParts;
	private Status status; // TODO: later an error message / exception stack trace can be incorporated

	public UploadDescriptor() {
		absolutePath = null;
		totalParts = bytesSoFar = partsSoFar = 0;
		status = Status.DOING;
	}

	public synchronized void setAbsolutePath(String filename) {
		this.absolutePath = filename;
	}

	public synchronized String getAbsolutePath() {
		return absolutePath;
	}

	public synchronized void increaseBytesSoFar(int moreBytes) {
		bytesSoFar += moreBytes;
	}

	public synchronized int getBytesSoFar() {
		return bytesSoFar;
	}
	
	public synchronized void setTotalBytes(int totalBytes) {
		this.totalBytes = totalBytes;
	}
	
	public int getTotalBytes() {
		return totalBytes;
	}

	public synchronized void incrementPartsSoFar() {
		++partsSoFar;
		if (partsSoFar == totalParts) {
			status = Status.DONE;
		}
	}

	public synchronized int getPartsSoFar() {
		return partsSoFar;
	}

	public synchronized void setTotalParts(int totalParts) {
		this.totalParts = totalParts;
	}

	public synchronized int getTotalParts() {
		return totalParts;
	}

	public synchronized double getPercentage() {
		// TODO: we are cheating here a bit, counting parts and not bytes
		// on the other hand, total bytes count ALL the bytes in request (including other form fields)
		// so the file size will be always less then form size, since a form usually includes the file and more fields
		// (such as input fields or "submit" button) 
		// Anyways, parts are good enough for big files at this stage and all the other info is already here 
		// to switch to more sophisticated/trustworthy calculation.
		return totalParts != 0 ? ((double) partsSoFar / (double) totalParts) : 0;
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized void setErrorStatus() {
		status = Status.ERROR;
	}

	public synchronized String toJsonString() {
		String statusString = new String();
		
		if (getStatus() == Status.DONE) {
			statusString = " , \"totalBytes\" : " + totalBytes;
		}
		return "{ \"status\" : \"" + getStatus() + "\", \"progress\" : " + getPercentage() + statusString + " }";
	}
}
