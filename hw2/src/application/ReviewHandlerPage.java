package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;

public class ReviewHandlerPage {
    private final DatabaseHelper databaseHelper;
    private User currentUser;
    private ObservableList<Review> myReviews = FXCollections.observableArrayList();
    private ListView<Review> reviewListView = new ListView<>(myReviews);
    
    // Message components as class fields
    private TextArea messageArea = new TextArea();
    private Button sendMessageButton = new Button("Send Feedback to Reviewer");
    private ListView<Message> messageListView = new ListView<>();
    
    public ReviewHandlerPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void start(Stage primaryStage, User user) {
        this.currentUser = user;
        primaryStage.setTitle("My Reviews");
        
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect to the database.");
            return;
        }

        // Main layout
        HBox root = new HBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(addReviewList(user), addReviewButtons(primaryStage, user));
        
        // Finish setting up scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public VBox addReviewList(User user) {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        
        reviewListView.setCellFactory(param -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                } else {
                    setText("Review ID: " + review.getId() + " - " + 
                           review.getReviewtext().substring(0, Math.min(50, review.getReviewtext().length())) + "...");
                }
            }
        });
                
        // Add selection listener to load messages when a review is selected
        reviewListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMessagesForReview(newVal);
            }
        });
        
        myReviews.clear();
        myReviews.addAll(databaseHelper.getMyReviews(databaseHelper.getUserId(user.getUserName())));
    
        leftPanel.getChildren().addAll(reviewListView, createMessagePanel());
        return leftPanel;
    }
        
    private VBox createMessagePanel() {
        VBox messagePanel = new VBox(10);
        messagePanel.setPadding(new Insets(10));
        
        Label messageLabel = new Label("Discussion about this review:");
        messageListView.setPrefHeight(200);
        
        messageArea.setPromptText("Type your feedback about this review...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(3);
        
        sendMessageButton.setOnAction(e -> sendMessageAboutReview());
        
        messagePanel.getChildren().addAll(messageLabel, messageListView, messageArea, sendMessageButton);
        return messagePanel;
    }
    
    private void loadMessagesForReview(Review review) {
        try {
            ObservableList<Message> messages = databaseHelper.getMessagesForReview(
                    review.getId(), 
                    databaseHelper.getUserId(currentUser.getUserName()));
            review.getMessages().setAll(messages);
            messageListView.setItems(review.getMessages());
            messageListView.setCellFactory(param -> new ListCell<Message>() {
                @Override
                protected void updateItem(Message message, boolean empty) {
                    super.updateItem(message, empty);
                    if (empty || message == null) {
                        setText(null);
                    } else {
                        String senderName = databaseHelper.getUserName(message.getSenderId());
                        setText(senderName + ": " + message.getMessageText());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load messages for this review");
        }
    }
    
    private void sendMessageAboutReview() {
        Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            showAlert("Error", "Please select a review first");
            return;
        }
        
        String messageText = messageArea.getText().trim();
        if (messageText.isEmpty()) {
            showAlert("Error", "Message cannot be empty");
            return;
        }
        
        try {
            // Get current user and reviewer IDs
            int currentUserId = databaseHelper.getUserId(currentUser.getUserName());
            int reviewerId = selectedReview.getUserId();
            
            databaseHelper.sendReviewMessage(
                currentUserId,
                reviewerId,
                selectedReview.getId(),
                messageText
            );
            
            // Refresh messages and clear input
            loadMessagesForReview(selectedReview);
            messageArea.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }
    
    public VBox addReviewButtons(Stage primaryStage, User user) {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setAlignment(Pos.TOP_CENTER);
        
        Button viewButton = new Button("View Full Review");
        Button updateButton = new Button("Edit Review");
        Button deleteButton = new Button("Delete Review");
        Button backButton = new Button("Back to Home");
        
        // Style buttons
        String buttonStyle = "-fx-pref-width: 150; -fx-pref-height: 30;";
        viewButton.setStyle(buttonStyle);
        updateButton.setStyle(buttonStyle);
        deleteButton.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);
        
        viewButton.setOnAction(a -> {
            Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                viewReviewHandler(selectedReview).show();
            } else {
                showAlert("Selection Error", "Please select a review to view");
            }
        });
        
        updateButton.setOnAction(a -> {
            Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                editReviewHandler(selectedReview, user).show();
            } else {
                showAlert("Selection Error", "Please select a review to edit");
            }
        });
        
        deleteButton.setOnAction(a -> {
            Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
            if (selectedReview != null) {
                deleteReviewHandler(selectedReview, user);
            } else {
                showAlert("Selection Error", "Please select a review to delete");
            }
        });
        
        backButton.setOnAction(a -> {
            new ReviewerHomePage(databaseHelper).show(primaryStage, user);
        });
        
        rightPanel.getChildren().addAll(viewButton, updateButton, deleteButton, backButton);
        return rightPanel;
    }

    private Stage viewReviewHandler(Review selectedReview) {
        Stage newStage = new Stage();
        VBox viewBox = new VBox(10);
        viewBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("Review Details");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        TextArea reviewText = new TextArea(selectedReview.getReviewtext());
        reviewText.setEditable(false);
        reviewText.setWrapText(true);
        reviewText.setPrefHeight(300);
        
        viewBox.getChildren().addAll(titleLabel, reviewText);
        Scene newScene = new Scene(viewBox, 500, 400);
        newStage.setScene(newScene);
        newStage.setTitle("Review Details");
        return newStage;
    }
    
    private Stage editReviewHandler(Review selectedReview, User user) {
        Stage newStage = new Stage();
        VBox editBox = new VBox(10);
        editBox.setPadding(new Insets(10));
        
        Label editLabel = new Label("Edit Review");
        editLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        TextArea newReview = new TextArea(selectedReview.getReviewtext());
        newReview.setWrapText(true);
        newReview.setPrefHeight(300);
        
        Button submit = new Button("Save Changes");
        submit.setStyle("-fx-pref-width: 120;");
        submit.setOnAction(b -> {
            String newText = newReview.getText().trim();
            if (!newText.isEmpty()) {
                try {
                    databaseHelper.updateReview(selectedReview.getId(), newText);
                    getMyReviews(user);
                    newStage.close();
                    showAlert("Success", "Review updated successfully");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to update review");
                }
            } else {
                showAlert("Input Error", "Cannot submit an empty review");
            }
        });
        
        editBox.getChildren().addAll(editLabel, newReview, submit);
        Scene newScene = new Scene(editBox, 500, 400);
        newStage.setScene(newScene);
        newStage.setTitle("Edit Review");
        return newStage;
    }
    
    private void deleteReviewHandler(Review selectedReview, User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Review");
        confirmation.setContentText("Are you sure you want to delete this review?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    databaseHelper.deleteReview(selectedReview.getId());
                    getMyReviews(user);
                    showAlert("Success", "Review deleted successfully");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to delete review");
                }
            }
        });
    }
    
    private void getMyReviews(User user) {
        myReviews.clear();
        myReviews.addAll(databaseHelper.getMyReviews(databaseHelper.getUserId(user.getUserName())));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}