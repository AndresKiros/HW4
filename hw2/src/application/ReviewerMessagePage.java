package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.Optional;

public class ReviewerMessagePage {
	// private instance variables
	private final DatabaseHelper dbHelper;
	private ObservableList<Review> allReviews = FXCollections.observableArrayList();
	private ListView<Review> reviewListView = new ListView<>(allReviews);
	private ListView<Message> messageListView = new ListView<>();
	
	// Constructor
	public ReviewerMessagePage(DatabaseHelper databaseHelper) {
	    this.dbHelper = databaseHelper;
	}
	
	// Entry point to show the messaging system UI for the reviewer
	public void start(Stage primaryStage, User user) {
	    primaryStage.setTitle("Reviewer Messaging System");
	    BorderPane root = new BorderPane();
	    root.setPadding(new Insets(10));
	
	    // list of reviews
	    VBox reviewPanel = createReviewPanel(user);
	    root.setLeft(reviewPanel);
	
	    // messages for selected review
	    VBox centerPanel = new VBox(10);
	    centerPanel.getChildren().addAll(new Label("Messages:"), messageListView);
	    root.setCenter(centerPanel);
	
	    // interation buttons
	    VBox rightPanel = setupButtons(user);
	    root.setRight(rightPanel);
	
	    Scene scene = new Scene(root, 800, 600);
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}
	
	// Creates the panel that shows the reviews made by the reviewer
	private VBox createReviewPanel(User user) {
	    VBox panel = new VBox(10);
	    panel.setPadding(new Insets(10));
	    
	    Label titleLabel = new Label("Your Reviews:");
	    
	    // Format how reviews appear in the ListView
	    reviewListView.setCellFactory(param -> new ListCell<Review>() {
	        @Override
	        protected void updateItem(Review review, boolean empty) {
	            super.updateItem(review, empty);
	            if (empty || review == null) {
	                setText(null);
	            } else {
	                setText("Reviewed: " + review.getReviewtext());
	            }
	        }
	    });
	    
	    // Load messages when a review is selected
	    reviewListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
	        if (newSelection != null) {
	            loadMessagesForReview(newSelection.getId(), user);
	        }
	    });
	    
	    // Populate reviews from database
	    loadReviews(user);
	    panel.getChildren().addAll(titleLabel, reviewListView);
	    return panel;
	}
	
	// Sets up buttons for replying and initiating private exchanges
	private VBox setupButtons(User user) {
	    VBox buttonPanel = new VBox(10);
	    buttonPanel.setPadding(new Insets(10));
	    
	    Button replyButton = new Button("Reply to Message");
	    replyButton.setOnAction(e -> handleReplyMessage(user));
	    
	    Button privateExchangeButton = new Button("Private Exchange");
	    privateExchangeButton.setOnAction(e -> handlePrivateExchange(user));
	    
	    buttonPanel.getChildren().addAll(replyButton, privateExchangeButton);
	    return buttonPanel;
	}
	
	// Loads reviews written by the current reviewer from the database
	private void loadReviews(User user) {
	    allReviews.clear();
	    allReviews.addAll(dbHelper.getMyReviews(dbHelper.getUserId(user.getUserName())));
	}
	
	// Loads messages related to a specific review
	private void loadMessagesForReview(int reviewId, User user) {
	    messageListView.setItems(dbHelper.getMessagesForReview(reviewId, dbHelper.getUserId(user.getUserName())));
	}
	
	// Handles replying to a selected message
	private void handleReplyMessage(User user) {
	    Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
	    if (selectedMessage != null) {
	        String replyText = showInputDialog("Reply to Message", "Enter your reply:");
	        if (replyText != null && !replyText.isEmpty()) {
	        	try {
	        		// Send reply to the original sender
	        		dbHelper.sendMessage(selectedMessage.getReceiverId(), selectedMessage.getSenderId(), selectedMessage.getQuestionId(), replyText);
	        		// Refresh message list
	        		loadMessagesForReview(selectedMessage.getQuestionId(), user);
	        	} catch (SQLException ex) {
	        		ex.printStackTrace();
                    showAlert("Database Error", "Failed to send reply.");
	        	}
	        } else {
	        showAlert("Selection Error", "Please select a message to reply to.");
	        }
	    }
	}
	
	// Allows a student to start a private exchange with a reviewer
	private void handlePrivateExchange(User user) {
	    String reviewerName = showInputDialog("Private Exchange", "Enter reviewer's name:");
	    if (reviewerName != null && !reviewerName.isEmpty()) {
	        int reviewerId = dbHelper.getUserId(reviewerName);
	        if (reviewerId != -1) {
	            String messageText = showInputDialog("Message", "Enter your message:");
	            if (messageText != null && !messageText.isEmpty()) {
	            	try {
	            	// Send a direct message with -1 for questionsId and indicating private context
	                dbHelper.sendMessage(dbHelper.getUserId(user.getUserName()), reviewerId, -1, messageText);
	            	} catch (SQLException ex) {
	            	ex.printStackTrace();
	            	showAlert("Database Error", "Failed to send direct message.");
	            	}
	            } else {
	            showAlert("User Not Found", "Reviewer not found.");
	            }
	        }
	    }
	}
	
	// Helper method to show an input dialog and return the result
	private String showInputDialog(String title, String prompt) {
	    TextInputDialog dialog = new TextInputDialog();
	    dialog.setTitle(title);
	    dialog.setHeaderText(prompt);
	    Optional<String> result = dialog.showAndWait();
	    return result.orElse(null);
	}
	
	// Helper method to show an alert dialog with an error message
	private void showAlert(String title, String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle(title);
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}
}
