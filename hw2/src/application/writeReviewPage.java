package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import java.sql.*;
import java.util.Optional;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class writeReviewPage {
	//private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseHelper databaseHelper;
    
    private ObservableList<Question> questionsInView = FXCollections.observableArrayList();
    private ListView<Question> questionListView = new ListView<>(questionsInView);
    private ListView<Answer> answerListView = new ListView<>();
    
    public writeReviewPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
		}
    
    public void start(Stage primaryStage,User user) {
    	primaryStage.setTitle("Write Reviews Here");
        // Connect to the database
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to connect to the database.");
            return;
        }

        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        root.setLeft(addReviewPanel(user));
        root.setCenter(addQuestionList());
        root.setRight(addAnswerList());
        root.setBottom(addBackButton(primaryStage, user));
        
      //finish setting up scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }
    
    //function to create an area to write a review and submit review
    public VBox addReviewPanel(User user) {
    	VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        
        Label writeLabel = new Label("Write a Review about Answer");
        TextArea reviewInput = new TextArea();
        reviewInput.setPromptText("Write your review here...");
        Button submitButton = new Button("Publish Review");
        
        submitButton.setOnAction(a -> {
        	Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
        	if(selectedAnswer != null) {
        		String reviewText = reviewInput.getText().trim();
        		if(!reviewText.isEmpty()) {
        			try {
        				int userId = databaseHelper.getUserId(user.getUserName());
        				databaseHelper.addReview(selectedAnswer.getId(), userId, reviewText);
        				reviewInput.clear();
        				//add Review to ReviewList
        				
        			} catch (SQLException ex) {
        				ex.printStackTrace();
        				showAlert("Database Error", "Failed to publish Review.");
        			}
        		} else { showAlert("Input Error", "Please enter an answer."); }
        	} else { showAlert("Selection Error", "Please select an answer to review."); }
        });
        
        leftPanel.getChildren().addAll(writeLabel, reviewInput, submitButton);
        return leftPanel;
    }
    
    //function to setup questionListView and question search bar
    public VBox addQuestionList() {
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
            	answerListView.setItems(databaseHelper.getAnswersForQuestion(newSelection.getId()));
            }
        });
        
        questionsInView.clear();
    	questionsInView.addAll(databaseHelper.getAllQuestions());
        
        // Center: Search for questions
        VBox questionBox = new VBox(10);
        TextField questionSearch = new TextField();
        questionSearch.setPromptText("Search questions...");
        questionSearch.setOnAction(e -> {
        	String searchText = questionSearch.getText().trim();
        	if(!searchText.isEmpty()) {
        		questionsInView.clear();
            	questionsInView.addAll(databaseHelper.searchQuestion(searchText));
        	}else { 
        		questionsInView.clear();
        		questionsInView.addAll(databaseHelper.getAllQuestions());
        		}
        });
        
        questionBox.getChildren().addAll(questionSearch, questionListView);
        return questionBox;
    }
    
    //function to setup answerListView
    public VBox addAnswerList(){

    	answerListView.setCellFactory(param -> new ListCell<Answer>() {
            @Override
            protected void updateItem(Answer answer, boolean empty) {
                super.updateItem(answer, empty);
                if (empty || answer == null) {
                    setText(null);
                } else {
                    setText("A: " + answer.getAnswerText());
                    }
            }
        });
    	
    	VBox answerBox = new VBox(10);
    	answerBox.setPadding(new Insets(10));
    	answerBox.getChildren().add(answerListView);
    	return answerBox;
    }
    
    //function to create a button to return to reviewerHomePage
    public HBox addBackButton(Stage primaryStage, User user) {
    	HBox back = new HBox(10);
    	back.setPadding(new Insets(10));
    	
    	Button backButton = new Button("Back");
 	    backButton.setOnAction(a -> {
 	    	new ReviewerHomePage(databaseHelper).show(primaryStage, user);
 	    });
 	    
 	    back.getChildren().add(backButton);
 	    back.setAlignment(Pos.CENTER_RIGHT);
 	    return back;
    }
    
    //function to handle all of the error messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


