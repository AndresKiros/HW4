package application;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private int questionId;
    private String messageText;
    private boolean isRead;

    // Constructor
    public Message(int id, int senderId, int receiverId, int questionId, String messageText, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.questionId = questionId;
        this.messageText = messageText;
        this.isRead = isRead;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    

    public int getSenderId() {
        return senderId;
    }

   
    
    public int getReceiverId() {
        return receiverId;
    }

    public int getQuestionId() {
    	return questionId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", messageText='" + messageText + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
