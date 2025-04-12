package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Review {
    private int id;
    private int answerId;
    private int userId;
    private String reviewText;
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    
    public Review(int id, int answerid, int userid, String reviewText) {
        this.id = id;
        this.answerId = answerid;
        this.userId = userid;
        this.reviewText = reviewText;
    }
    
    public int getId() { return id; }
    public int getAnswerId() { return answerId; }
    public int getUserId() { return userId; }
    public String getReviewtext() { return reviewText; }
    public ObservableList<Message> getMessages() { return messages; }
    
    // Add setter for messages if needed
    public void setMessages(ObservableList<Message> messages) {
        this.messages.setAll(messages);
    }
}