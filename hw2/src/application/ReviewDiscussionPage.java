package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;

public class ReviewDiscussionPage {
    private final DatabaseHelper dbHelper;
    private User currentUser;
    private Review currentReview;
    
    private ListView<Message> messageListView = new ListView<>();
    private TextArea messageArea = new TextArea();
    
    public ReviewDiscussionPage(DatabaseHelper dbHelper) {
    	
        this.dbHelper = dbHelper;
    }
    
    public void start(Stage stage, User user, Review review) {
    	// Add null checks at the start
        if (review == null) {
            showAlert("Error", "No review selected");
            return;
        }
    	
        this.currentUser = user;
        this.currentReview = review;
        stage.setTitle("Review Discussion - Review ID: " + review.getId());
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Review display
        TextArea reviewDisplay = new TextArea(review.getReviewtext());
        reviewDisplay.setEditable(false);
        reviewDisplay.setWrapText(true);
        
        // Message list
        messageListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    String sender = dbHelper.getUserName(message.getSenderId());
					setText(sender + ": " + message.getMessageText());
                }
            }
        });
        
        // Message input
        messageArea.setPromptText("Type your feedback here...");
        messageArea.setWrapText(true);
        
        Button sendButton = new Button("Send Feedback");
        sendButton.setOnAction(e -> sendMessage());
        
        // Layout
        VBox reviewBox = new VBox(5, new Label("Original Review:"), reviewDisplay);
        VBox messageBox = new VBox(5, new Label("Discussion:"), messageListView);
        VBox inputBox = new VBox(5, messageArea, sendButton);
        
        root.getChildren().addAll(reviewBox, messageBox, inputBox);
        
        // Load initial messages
        loadMessages();
        
        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }
    
    private void loadMessages() {
        // Get user ID from database using username
		int userId = dbHelper.getUserId(currentUser.getUserName());
		
		ObservableList<Message> messages = dbHelper.getMessagesForReview(
		    currentReview.getId(), 
		    userId
		);
		messageListView.setItems(messages);
    }
    
    private void sendMessage() {
        String text = messageArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Error", "Message cannot be empty");
            return;
        }

        try {
            // Get current user's ID from database
            int senderId = dbHelper.getUserId(currentUser.getUserName());
            
            dbHelper.sendReviewMessage(
                senderId,                    // Current user's ID
                currentReview.getUserId(),    // Reviewer's ID
                currentReview.getId(),        // Review ID
                text
            );
            
            // Refresh messages and clear input
            loadMessages();
            messageArea.clear();
        } catch (SQLException e) {
            showAlert("Error", "Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}