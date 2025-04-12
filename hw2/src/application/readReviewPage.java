package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class readReviewPage {
private final DatabaseHelper databaseHelper;
    
    private ObservableList<Question> questionsInView = FXCollections.observableArrayList();
    private ListView<Question> questionListView = new ListView<>(questionsInView);
    private ListView<Answer> answerListView = new ListView<>();
    private ListView<Review> reviewListView = new ListView<>();
    
    private boolean viewTrusted = false;
    
    public readReviewPage(DatabaseHelper databaseHelper) {
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
        
        root.setLeft(addQuestionList());
        root.setCenter(addAnswerList());
        root.setRight(addReviewList(user));
        root.setBottom(addBackButton(primaryStage, user));
        
      //finish setting up scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
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
    	answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
            	reviewListView.setItems(databaseHelper.getReviewsForAnswer(newSelection.getId()));
            }
        });
    	
    	VBox answerBox = new VBox(10);
    	answerBox.setPadding(new Insets(10));
    	answerBox.getChildren().add(answerListView);
    	return answerBox;
    }
    
    public VBox addReviewList(User user) {
    	reviewListView.setCellFactory(param -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                } else {
                    setText(user.getUserName() + ": " + review.getReviewtext());
                }
            }
        });
    	
    	Label reviewLabel = new Label("Reviews for Answer:");
    	Button trustButton = new Button("Mark Reviewer as Trusted");
        Button toggleUnresolvedButton = new Button("Filter by Trusted Reviewers");
        
        toggleUnresolvedButton.setOnAction(e -> {
            if (viewTrusted) {
                try {
            	databaseHelper.getAllReviews();
                toggleUnresolvedButton.setText("Filter by Trusted Reviewers");
                } catch (SQLException ex){
                	ex.printStackTrace();
                	showAlert("Database Error", "Could not filter by trusted reviewers");
                }
            } else {
            	try {
            	databaseHelper.filterTrusted();
                toggleUnresolvedButton.setText("Unfilter");
            	} catch (SQLException ex){
            		ex.printStackTrace();
            		showAlert("Database Error", "Could not unfilter");
            	}
            }
            viewTrusted = !viewTrusted;
        });
    	trustButton.setOnAction(a -> {
    		Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
    		if(selectedReview != null) {
    			try {
    				databaseHelper.markTrusted(selectedReview.getUserId());
    			} catch (SQLException ex) {
    					ex.printStackTrace();
    					showAlert("Database Error", "Failed to trust Reviewer.");
    			}
    		}
    	});
    	
    	VBox reviewBox = new VBox(10);
    	reviewBox.setPadding(new Insets(10));
    	reviewBox.getChildren().addAll(reviewLabel, reviewListView, trustButton);
    	return reviewBox;
    }
    //function to create a button to return to reviewerHomePage
    public HBox addBackButton(Stage primaryStage, User user) {
    	HBox back = new HBox(10);
    	back.setPadding(new Insets(10));
    	
    	Button backButton = new Button("Back");
 	    backButton.setOnAction(a -> {
 	    	new StudentHomePage(databaseHelper).show(primaryStage, user);
 	    });
 	    
 	    back.getChildren().add(backButton);
 	    back.setAlignment(Pos.CENTER_RIGHT);
 	    return back;
    }
      //Gives useful error messages
        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
}
