package test;

// needs to be synchronized, since more then a single servlet (execution context) instance might access the same descriptor at once
public class UploadDescriptor implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status { UNKNOWN, DOING, DONE, ERROR };
	
	public static final UploadDescriptor UNKNOWN; // kinda NULL/notfound descriptor
	static {
		UNKNOWN = new UploadDescriptor(null, 0);
		UNKNOWN.status = Status.UNKNOWN;
	}
	
	private String absolutePath;
	private int bytesSoFar;
	private int partsSoFar;
	private int totalParts;
	private Status status; // TODO: later an error message / exception stack trace can be encorporated
	
	public UploadDescriptor(String absolutePath, int totalParts) {
		setAbsolutePath(absolutePath);
		setTotalParts(totalParts); 
		bytesSoFar = partsSoFar = 0;
		status = Status.DOING;
	}
	
	private synchronized void setAbsolutePath(String filename) {
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
	
	public synchronized void incrementPartsSoFar() {
		++partsSoFar;
		if (partsSoFar == totalParts) {
			status = Status.DONE;
		}
	}
	
	public synchronized int getPartsSoFar() {
		return partsSoFar;
	}

	private synchronized void setTotalParts(int totalParts) {
		this.totalParts = totalParts;
	}

	public synchronized int getTotalParts() {
		return totalParts;
	}
	
	public synchronized double getPercentage() {
		return totalParts != 0 ? ((double)partsSoFar/(double)totalParts) : 0;
	}
	
	public synchronized Status getStatus() {
		return status;
	}
	
	public synchronized void setErrorStatus() {
		status = Status.ERROR;
	}
	
	public synchronized String toJsonString() {
		return "{ \"status\" : \"" + getStatus() + "\", \"progress\" : " + getPercentage() + " }";
	}
}
