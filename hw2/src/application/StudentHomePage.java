package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * This page displays a simple welcome message for the user.
 */

public class StudentHomePage {

	
	 private ObservableList<Message> unreadMessages = FXCollections.observableArrayList();
	 private ListView<Message> messageListView = new ListView<>(unreadMessages);
	 
	 private ObservableList<Question> myQuestions = FXCollections.observableArrayList();
	 private ListView<Question> questionListView = new ListView<>(myQuestions);
	    
    private final DatabaseHelper databaseHelper;
    
    private ListView<Review> reviewListView = new ListView<>();
    private ListView<Message> reviewMessageListView = new ListView<>();
    private TextArea reviewMessageArea = new TextArea();
    private Review currentReview;
    private VBox reviewDiscussionPanel;

    public StudentHomePage(DatabaseHelper databaseHelper) {
    	this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage,User user) {
		
		//setTitle for StudentHomePage
		primaryStage.setTitle("Student Page");
		
		//create borderPane for home page
		BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
		
        //will have buttons on left side of home page to allow user to go to different pages and perform different functions
		VBox Options = new VBox();
	    Options.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Student!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    //Take you to question page
	    Button questionsButton = new Button("Questions");
		questionsButton.setOnAction(a -> {
			new QuestionAndAnswerPage(databaseHelper).start(primaryStage, user);
		});
	    
		//Takes you to messages page
		Button messagesButton = new Button("Messages");
		messagesButton.setOnAction(a -> {
			new MessagePage(databaseHelper).start(primaryStage, user);
		});
		
	    // Takes you to login page
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> {
	    	new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    
	    Button reviewDiscussionsButton = new Button("Review Discussions");
	    reviewDiscussionsButton.setOnAction(a -> {
	        // Get all available reviews
	        ObservableList<Review> allReviews = null;
			try {
				allReviews = databaseHelper.getAllReviews();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        if (allReviews.isEmpty()) {
	            showAlert("No Reviews", "There are no reviews available yet", "information");
	            return;
	        }
	        
	        // Open discussion page with the first available review
	        new ReviewDiscussionPage(databaseHelper).start(new Stage(), user, allReviews.get(0));
	    });
	    
	    Button readReview = new Button("Reviews");
	    readReview.setOnAction(a -> {
	    	new readReviewPage(databaseHelper).start(primaryStage, user);
	    });
	    
	    //put objects into VBox and insert VBox into borderPane
	    Options.getChildren().addAll(userLabel,questionsButton, messagesButton,readReview, reviewDiscussionsButton, logoutButton);
	    root.setLeft(Options);
	    
	    Button requestReviewer = new Button("Request to be reviewer");
	    
	    //Grayed out if already requested
	   // if(databaseHelper.viewRequest())
	    requestReviewer.setOnAction(a -> {
	    	requestReviewer.setDisable(true);
	    	showAlert("Request Sent","Request to change role to reviewer has been sent.","information");
	    	try {
				databaseHelper.roleChangeRequest(user, "Reviewer");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    });
	    
	    //set listView up to show questions 
        questionListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) {
                    setText(null);
                } else {
                    setText("Q: " + question.getQuestionText());
                }
            }
        });
        //when selecting a question, will load all unread Messages for question
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
            	
            	loadUnreadMessages(user, questionListView.getSelectionModel().getSelectedItem().getId());
                
            }
        });
    
        //I am going to comment this out because it doesn't make sense.
        /*
        //loads questionListView with questions user wrote
        loadMyQuestions(user);
        
        //create search bar to look for specific questions
        TextField questionSearch = new TextField();
        questionSearch.setPromptText("Search questions...");
        questionSearch.setOnAction(e -> {
        	String searchText = questionSearch.getText().trim();
        	if(!searchText.isEmpty()) {
        		questionLookup(searchText);
        	}else { loadMyQuestions(user); }
        });
      
        //add listView and searchbar to a VBox, and then add to borderPane
        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(questionSearch, questionListView);
        root.setCenter(centerBox);
	      */
	    //create new VBox that holds unread messages and mark as read button
	    VBox Messages = new VBox();
	    Messages.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    //set parameters for listView object
	    messageListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                	//will show sender's userName and then their message
                    setText(databaseHelper.getUserName(message.getSenderId()) + ": " + message.getMessageText());
                }
            }
        });
	    
	    //mark message as read
        Button readMessageButton = new Button("Mark as Read");
        readMessageButton.setOnAction(e -> handleReadMessage(user));
        
        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Enter your reply here...");
        Button replyButton = new Button("Send Reply");
        
        
        replyButton.setOnAction(e -> {
        	//will take selected Message from the messageListView
            Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
            if (selectedMessage != null) {
                String messageText = messageInput.getText().trim();
                if (!messageText.isEmpty()) {
                    try {
                    	//will send reply to user who originally sent message
                        databaseHelper.sendMessage(databaseHelper.getUserId(user.getUserName()),
                        		selectedMessage.getSenderId(),
                        		selectedMessage.getQuestionId(),
                        		messageText);
                        
                        
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Database Error", "Failed to send message.");
                    }
                } else {
                    showAlert("Input Error", "Please enter a message.");
                }
            } else {
                showAlert("Selection Error", "Please select a message to reply to.");
            }
        });
        
        //add listView and mark as read button to borderPane
        Messages.getChildren().addAll(new Label("My unread messages"), messageListView, readMessageButton, messageInput, replyButton, requestReviewer);
        root.setRight(Messages);
        
        //create Scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
	
	//will mark a message as read
	private void handleReadMessage(User user) {
    	//pull Answer object from listView
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
                try {
                	//will mark question as read and remove it from list
                    databaseHelper.markAsRead(selectedMessage.getId());
                    loadUnreadMessages(user, selectedMessage.getQuestionId());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to Mark as Read.");
                }
        } else {
            showAlert("Selection Error", "Please select a message first.");
        }
    }
	
	 //function to handle all of the error messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlert(String title, String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //will load all unread messages
    private void loadUnreadMessages(User user, int questionId) {
    	unreadMessages.clear();
    	//load in unread messages into ObservableList so they will show in listView 
        unreadMessages.addAll(databaseHelper.getUnreadMessages(databaseHelper.getUserId(user.getUserName()), questionId));
    }

  //Shows search questions
    private void questionLookup(String question) {
    	myQuestions.clear();
    	myQuestions.addAll(databaseHelper.searchQuestion(question));
    }
    
    //will load all questions user has wrote to view unread messages
    private void loadMyQuestions(User user) {
    	myQuestions.addAll(databaseHelper.getMyQuestions(databaseHelper.getUserId(user.getUserName())));
    }
    







}

