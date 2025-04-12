package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import java.sql.*;
import java.util.Optional;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
public class MessagePage {

	//private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseHelper dbHelper;

    private ObservableList<Question> allQuestions = FXCollections.observableArrayList();
    private ListView<Question> questionListView = new ListView<>(allQuestions);
    private ListView<Message> messageListView = new ListView<>();
    
    public MessagePage(DatabaseHelper databaseHelper) {
		this.dbHelper = databaseHelper;
		}
    
    
    public void start(Stage primaryStage,User user) {
    	primaryStage.setTitle("Messaging System");
        // Connect to the database
        try {
            dbHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect to the database.");
            return;
        }

        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        //add panel to write a question to left side of borderPane
        VBox messagePanel = createMessagePanel(primaryStage, user);
        root.setLeft(messagePanel);
        
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
        
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
            	System.out.println(dbHelper.getUserName(newSelection.getUserId()));
                loadMessagesForQuestion(newSelection.getId(), user);
            }
        });
        
        //will first show all unresolved questions if nothing is typed into search bar
        loadOtherQuestions(user);
        
        //create search bar to look for specific questions
        TextField questionSearch = new TextField();
        questionSearch.setPromptText("Search questions...");
        questionSearch.setOnAction(e -> {
        	String searchText = questionSearch.getText().trim();
        	if(!searchText.isEmpty()) {
        		questionLookup(searchText);
        	}else { loadOtherQuestions(user); }
        });
        
        //add listView and searchbar to a VBox, and then add to borderPane
        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(questionSearch,new Label("Other User's Questions") ,questionListView);
        root.setCenter(centerBox);
        
        //set up messages listView to show messages related to question
        messageListView.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                	//will show sender's userName and then their message
                    setText(dbHelper.getUserName(message.getSenderId()) + ": " + message.getMessageText());
                }
            }
        });
        
        //back button to return to home page
        Button backButton = new Button("Back");
	    backButton.setOnAction(a -> {
			if(user.getRole().equals("reviewer")) {
				new ReviewerHomePage(dbHelper).show(primaryStage, user);
		    }else{
		    	new StudentHomePage(dbHelper).show(primaryStage, user);
		    }});
	    
        //add listView and buttons to right of Vbox
        VBox rightBox = new VBox(10);
        rightBox.getChildren().addAll(new Label("Messages: "), messageListView);
        setupButtons(rightBox, user);
        rightBox.getChildren().add(backButton);
        
        //add VBox to borderPane
        root.setRight(rightBox);
        
        //finish setting up scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }
    
    private VBox createMessagePanel(Stage primaryStage, User user) {
    	VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        //create area to ask a question and submit question
        Label titleLabel = new Label("Send a message about a question");
        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Enter your message here...");
        Button submitButton = new Button("Send Message");
        
        
        submitButton.setOnAction(e -> {
        	//will take selected Question from the questionListView
            Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                String messageText = messageInput.getText().trim();
                if (!messageText.isEmpty()) {
                    try {
                    	//will send message to database and to other user
                        dbHelper.sendMessage(dbHelper.getUserId(user.getUserName()),
                        		dbHelper.getUserIdFromQuestion(selectedQuestion.getId()),
                        		selectedQuestion.getId(),
                        		messageText);
                        System.out.println(dbHelper.getUserName(dbHelper.getUserIdFromQuestion(selectedQuestion.getId())));
                        //clears textArea and updates messageListView
                        messageInput.clear();
                        loadMessagesForQuestion(selectedQuestion.getId(), user);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Database Error", "Failed to add message.");
                    }
                } else {
                    showAlert("Input Error", "Please enter a message.");
                }
            } else {
                showAlert("Selection Error", "Please select a question to write about.");
            }
        });

        panel.getChildren().addAll(titleLabel, messageInput,  submitButton);
        return panel;
    }
    
    //function to add delete and update buttons for messages
    private void setupButtons(VBox rightBox, User user) {

        // Delete Message Button
        Button deleteMessageButton = new Button("Delete Message");
        deleteMessageButton.setOnAction(e -> handleDeleteMessage(user));

        // Update MEssage Button
        Button updateMessageButton = new Button("Update Message");
        updateMessageButton.setOnAction(e -> handleUpdateMessage(user));
	    
        
        // Add buttons to the UI layout
        VBox buttonPanel = new VBox(10, deleteMessageButton, updateMessageButton);
        buttonPanel.setPadding(new Insets(10));

        // Add the button panel to the main layout
        rightBox.getChildren().add(buttonPanel);
    }
    
    //function to handle all of the error messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //function to update messageListView with messages I sent to user about the question
    private void loadMessagesForQuestion(int questionId, User user) {
        messageListView.setItems(dbHelper.getMessagesForQuestion(dbHelper.getUserId(user.getUserName()), questionId));
    }
    
    //function to update questionListView questions other users have wrote
    private void loadOtherQuestions(User user) {
        allQuestions.clear();
        allQuestions.addAll(dbHelper.getOtherQuestions(dbHelper.getUserId(user.getUserName())));
    }
    
    //Shows search questions
    private void questionLookup(String question) {
    	allQuestions.clear();
    	allQuestions.addAll(dbHelper.searchQuestion(question));
    }
    
    //will delete a message and update listView
    private void handleDeleteMessage(User user) {
    	Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
            try {
                dbHelper.deleteMessage(selectedMessage.getId());
                loadMessagesForQuestion(selectedMessage.getQuestionId(), user); // Refresh the list of messages
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Database Error", "Failed to delete message.");
            }
        } else {
            showAlert("Selection Error", "Please select a message to delete.");
        }
    }

    private void handleReadMessage(User user) {
    	//pull Answer object from listView
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
                try {
                	//will mark question as read
                    dbHelper.markAsRead(selectedMessage.getId());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to update message.");
                }
        } else {
            showAlert("Selection Error", "Please select an message to update.");
        }
    }
    
    //will update a message and listView
    private void handleUpdateMessage(User user) {
    	//pull Answer object from listView
        Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
        if (selectedMessage != null) {
        	//pop up new window to update answer, and input new answer into Answer object
            String newMessageText = showUpdateDialog(selectedMessage.getMessageText());
            if (newMessageText != null && !newMessageText.isEmpty()) {
                try {
                    dbHelper.updateMessage(selectedMessage.getId(),newMessageText);
                    loadMessagesForQuestion(selectedMessage.getQuestionId(), user); // Refresh the list of answers
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to update message.");
                }
            }
        } else {
            showAlert("Selection Error", "Please select an message to update.");
        }
    }
    
    //popup window to input updates to messages
    private String showUpdateDialog(String currentText) {
        TextInputDialog dialog = new TextInputDialog(currentText);
        dialog.setTitle("Update");
        dialog.setHeaderText("Enter the new text:");
        dialog.setContentText("Text:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    














}
