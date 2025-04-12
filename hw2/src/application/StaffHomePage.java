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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * This page displays a simple welcome message for the user.
 */

public class StaffHomePage {
	
	//Variables
	private final DatabaseHelper databaseHelper;
	private ObservableList<Question> questionsInView = FXCollections.observableArrayList();
    private ListView<Question> questionListView = new ListView<>(questionsInView);
    private ListView<Answer> answerListView = new ListView<>();
    private TextField questionTextField = new TextField();
    private int questionUserId;
    private String questionUserName = null;
    private Label questionUserNameL = new Label();
    private Label questionUserRole = new Label();
    private Label answerUserNameL = new Label();
    private Label answerUserRole = new Label();
    
	public StaffHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage, User user) {
	    BorderPane layout = new BorderPane();
	    layout.setPadding(new Insets(20));

	    Scene userScene = new Scene(layout, 1000, 600);
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Staff Page");

	    // ==== Top Area (Header) ====
	    Label userLabel = new Label("Hello, Staff!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    layout.setTop(userLabel);

	    // ==== Left Side (List of Questions) ====
	    questionListView.setPrefWidth(300);
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
	    //Updater
	    questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
	        if (newSelection != null) {
	            questionTextField.setText(newSelection.getQuestionText()); 
	            loadAnswersForQuestion(newSelection.getId());
	            questionUserId = newSelection.getUserId();
	            questionUserName = databaseHelper.getUserName(questionUserId);
	            questionUserNameL.setText("User name: " + databaseHelper.getUserName(questionUserId));
	            questionUserRole.setText("Role: "  + databaseHelper.getUserRole(questionUserName));
	            
	            //debug
	            
	            System.out.println("Selected Question ID: " + newSelection.getId());
	            System.out.println("User ID: " + questionUserId);
	            System.out.println("User name: " + questionUserName);
	            System.out.println("User role: " + databaseHelper.getUserRole(questionUserName));
	        }
	    });
	    
	    answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
	        if (newSelection != null) {

	            answerUserNameL.setText("User name: " + databaseHelper.getUserName(questionUserId));
	            answerUserRole.setText("User role: " + databaseHelper.getUserRole(databaseHelper.getUserName(questionUserId)));

	            
	            answerListView.setOnMouseClicked(event -> {
	                if (event.getClickCount() == 2) {
	                    Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
	    	            System.out.println("Selected Answer ID: " + newSelection.getId());
	    	            System.out.println("Answer Text: " + newSelection.getAnswerText());
	    	            System.out.println("Username: " + databaseHelper.getUserName(newSelection.getUserId())); 
	    	            System.out.println("Accepted: " + newSelection.isAccepted());
        	            Answer tempAns = new Answer(newSelection.getId(),newSelection.getQuestionId(),newSelection.getUserId(),newSelection.getAnswerText(),newSelection.isAccepted());
                        try {
							showCommentPopup(tempAns, user);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 

	                    if (selectedAnswer != null) {

	                    }
	                }
	            });
	            
	            
	        }
	    });
	    layout.setLeft(questionListView);

	    // ==== Center (Answers) ====
	    answerListView.setCellFactory(param -> new ListCell<Answer>() {
	        @Override
	        protected void updateItem(Answer answer, boolean empty) {
	            super.updateItem(answer, empty);
	            if (empty || answer == null) {
	                setText(null);
	            } else {
	                setText(answer.getAnswerText()); // Display only the answer text
	            }
	        }
	    });
	    VBox centerContent = new VBox(10);
	    answerListView.setPrefWidth(400);
	    //Question!
	    questionTextField.setPrefWidth(200);
	    questionTextField.setPrefHeight(80);
	    Label questionTitle = new Label("Question");
	    questionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    //Answer!
	    Label answerTitle = new Label("Answer");
	    answerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    centerContent.setMaxWidth(800);
	    centerContent.getChildren().addAll(questionTitle,questionTextField,answerTitle,answerListView);
	    layout.setCenter(centerContent);
	    
	    // ==== Right Side (Actions / Logout) ====
	    VBox rightPanel = new VBox(10);
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> new UserLoginPage(databaseHelper).show(primaryStage));
	    Label QuestionTitle = new Label("Question");
	    QuestionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    questionUserNameL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    questionUserRole.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
	    questionUserNameL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    questionUserRole.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");


	    
	    rightPanel.getChildren().addAll(questionTitle,questionUserNameL,questionUserRole,logoutButton,answerTitle,answerUserNameL);
	    layout.setRight(rightPanel);
	    
	    loadAllQuestions();
	}
		

	
    //Refreshes UI to show changes in answers
    private void loadAnswersForQuestion(int questionId) {
        answerListView.setItems(databaseHelper.getAnswersForQuestion(questionId));
    }
    
    private void loadAllQuestions() {
    	questionsInView.clear();
    	questionsInView.addAll(databaseHelper.getAllQuestions());
    }
    
    private void showCommentPopup(Answer answer, User user) throws SQLException {
    	//Layout details
    	Stage commentPopup = new Stage();
    	commentPopup.setTitle("Comment details");
    	
    	//Answer:
    	Label answerTitle = new Label("Answer by User: " + databaseHelper.getUserName(answer.getUserId()));
    	answerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    	TextArea answerTextArea = new TextArea(answer.getAnswerText());
        answerTextArea.setEditable(false);
        answerTextArea.setMaxHeight(200);
        
        //Comment:
        Label commentLabel = new Label("Write comment here:");
        commentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        TextField commentTextField = new TextField(databaseHelper.getComment(answer.getId()));
        Button confirmButton = new Button("Confirm comment");
        
        confirmButton.setOnAction(a -> {
            String commentText = commentTextField.getText();
            if (!commentText.trim().isEmpty()) {
                try {
                    String createdAt = java.time.LocalDateTime.now().toString();
                    int isQuestion = 0; 
                    databaseHelper.addComment(user, commentText, answer.getId(), createdAt, isQuestion);
                    commentPopup.close(); 
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Empty comment. Nothing saved.");
            }
        });
        
        VBox popupLayout = new VBox(10);
        popupLayout.getChildren().add(answerTitle);
        popupLayout.getChildren().add(answerTextArea);
        popupLayout.getChildren().add(commentLabel);
        popupLayout.getChildren().add(commentTextField);
        popupLayout.getChildren().add(confirmButton);
        
        Scene popupScene = new Scene(popupLayout, 400, 300);
        popupLayout.setAlignment(Pos.CENTER);
        commentPopup.setScene(popupScene);
        commentPopup.show();
        
    }
	
}