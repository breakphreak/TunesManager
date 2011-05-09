package challenge;

// needs to be synchronized, since can be accessed from more then a single doGet/doPost() that run in parallel
public class UploadDescriptor implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		DOING, DONE, ERROR
	};

	private String absolutePath;
	private int bytesSoFar;
	private int totalBytes;
	private int partsSoFar;
	private int totalParts;
	private Status status; // TODO: later an error message / exception stack trace can be incorporated
	private String userComment;

	public UploadDescriptor() {
		userComment = absolutePath = null;
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

	public synchronized int getPercentage() {
		// TODO: we are cheating here a bit:
		// COMPLETION (100% only) is counted when all the parts are received
		// ONGOING PERCENTAGE/PROGRESS is counted as long as bytes are received
		// REASON: currently total bytes count ALL the bytes in request (including other form fields etc)
		// so the file size will be always LESS then request body size. However, the multipart request is received fully
		// only when all its parts are received, hence the main condition (totalParts == partsSoFar).
		return (
				(getTotalParts() == getPartsSoFar()) 
					? 100 
					: (int)(getBytesSoFar()*100/getTotalBytes())
		);
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
			statusString = " , \"totalBytes\" : " + getTotalBytes();
		}
		
		String userCommentString = new String();
		if (getUserComment() != null)  {
			userCommentString = " , \"userComment\" : \"" + getUserComment() + "\"";
		}
		
		return "{ \"status\" : \"" + getStatus() + "\", \"progress\" : " + getPercentage() + statusString + userCommentString + " }";
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment.trim(); 
	}

	public String getUserComment() {
		return userComment;
	}
}
