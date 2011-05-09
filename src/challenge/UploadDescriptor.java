package challenge;

// needs to be synchronized, since can be accessed from more then a single doGet/doPost() that run in parallel
public class UploadDescriptor implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public enum Status {
		DOING, DONE, ERROR
	};

	private String absolutePath;
	private long bytesSoFar;
	private long totalBytes;
	private Status status; // TODO: later an error message / exception stack trace can be incorporated
	private String userComment;

	public UploadDescriptor() {
		userComment = absolutePath = null;
		bytesSoFar = 0;
		status = Status.DOING;
	}

	public synchronized void setAbsolutePath(String filename) {
		this.absolutePath = filename;
	}

	public synchronized String getAbsolutePath() {
		return absolutePath;
	}

	public synchronized void increaseBytesSoFar(long moreBytes) {
		bytesSoFar += moreBytes;
	}

	public synchronized long getBytesSoFar() {
		return bytesSoFar;
	}
	
	public synchronized void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	
	public long getTotalBytes() {
		return totalBytes;
	}

	public synchronized int getPercentage() {
		// well, we are cheating here a bit:
		// COMPLETION (100% only) is counted when all the multipart-data HTTP request parts are received
		// ONGOING PERCENTAGE/PROGRESS is counted as long as bytes are received
		// REASON: currently total bytes count ALL the bytes in request (including other form fields etc)
		// so the file size will be always LESS then request body size. However, the multipart request is received fully
		// only when all its parts are received, and setDoneStatus() will be called.
		
		int percentage = 0;
		switch(getStatus()) {
		case DONE: 
			percentage = 100;
			break;

		case ERROR:			
		case DOING:
			percentage = getTotalBytes() == 0 ? 0 : (int)(getBytesSoFar()*100/getTotalBytes());
			break;		
		}
		
		assert(percentage >= 0 && percentage <= 100);
		return percentage;
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized void setDoneStatus() {
		status = Status.DONE;
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
