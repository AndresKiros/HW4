package application;
import java.time.LocalDateTime;


public class StaffComment {
	private String staffName;
	private String comment;
	private String genericId;
	private String answerId;
	private LocalDateTime createdAt;
	private boolean isQuestion;
	
	public StaffComment(String staffName, String comment, String genericId, LocalDateTime createdAt, boolean isQuestion) {
		this.staffName = staffName;
		this.comment = comment;
		this.genericId = genericId;
		this.createdAt = createdAt;
		this.isQuestion = isQuestion;
	}
	
	
	
	public String getStaffName() {
	    return staffName;
	}

	public void setStaffName(String staffName) {
	    this.staffName = staffName;
	}

	public String getComment() {
	    return comment;
	}

	public void setComment(String comment) {
	    this.comment = comment;
	}

	public String getGenericId() {
	    return genericId;
	}

	public void setGenericId(String genericId) {
	    this.genericId = genericId;
	}
	public boolean isQuestion() {
		return isQuestion;
	}
}
