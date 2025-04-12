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

public class QuestionAndAnswerPage{

    //private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseHelper dbHelper;

    private ObservableList<Question> questionsInView = FXCollections.observableArrayList();
    private ListView<Question> questionListView = new ListView<>(questionsInView);
    private ListView<Answer> answerListView = new ListView<>();

    //public static void main(String[] args) {
      //  launch(args);
    //}
    
    private boolean areUnresolvedQuestionsVisible = false;
    
    public QuestionAndAnswerPage(DatabaseHelper databaseHelper) {
		this.dbHelper = databaseHelper;
		}

	public void start(Stage primaryStage,User user) {
        primaryStage.setTitle("Student Question and Answer System");
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
        
        // Button to toggle unresolved questions
        Button toggleUnresolvedButton = new Button("Show Unresolved Questions");
        
        toggleUnresolvedButton.setOnAction(e -> {
            if (areUnresolvedQuestionsVisible) {
                hideUnresolvedQuestions();
                toggleUnresolvedButton.setText("Show Unresolved Questions");
            } else {
            	showUnresolvedQuestions();
                toggleUnresolvedButton.setText("Hide Unresolved Questions");
            }
            areUnresolvedQuestionsVisible = !areUnresolvedQuestionsVisible;
        });
        
        // Layout setup
        VBox topPanel = new VBox(10, toggleUnresolvedButton);
        topPanel.setPadding(new Insets(10));
        root.setTop(topPanel);
        
        //Answer checkbox
        CheckBox answerAccepted = new CheckBox();

        // Left side: Add Question Panel
        VBox addQuestionPanel = createAddQuestionPanel(primaryStage, user);
        root.setLeft(addQuestionPanel);
         
        answerListView.setCellFactory(param -> new ListCell<Answer>() {
            @Override
            protected void updateItem(Answer answer, boolean empty) {
                super.updateItem(answer, empty);
                if (empty || answer == null) {
                    setText(null);
                } else {
                    setText("A: " + answer.getAnswerText());
                    
                    answerAccepted.setSelected(answer.isAccepted()); // Checks if answer has been accepted.
                    answerAccepted.setDisable(true); // Viewers cannot edit the check box.
                    setGraphic(answerAccepted); // ListCell won't allow UI element without this.
                }
            }
        });
        
        

        // Center: List of Unresolved Questions
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
                loadAnswersForQuestion(newSelection.getId());
            }
        });
        
       

        // Center: Search for questions
        VBox centerBox = new VBox(10);
        TextField questionSearch = new TextField();
        questionSearch.setPromptText("Search questions...");
        questionSearch.setOnAction(e -> {
        	String searchText = questionSearch.getText().trim();
        	if(!searchText.isEmpty()) {
        		questionLookup(searchText);
        	}else {loadAllQuestions();}
        });
        centerBox.getChildren().addAll(questionSearch, questionListView);
        root.setCenter(centerBox);
        
        // Right side: Add Answer Panel and Answers List
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        
        //Add Answer Panel
        VBox answerPanel = createAnswerPanel(user);
        rightPanel.getChildren().add(answerPanel);
        
        // Add Buttons
        setupButtons(rightPanel);
        
        //Set the panel to the right
        root.setRight(rightPanel);

        // Load unresolved questions
        loadAllQuestions();      
        

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }
	
	private void showUnresolvedQuestions(){
		questionsInView.clear();
		questionsInView.addAll(dbHelper.getUnresolvedQuestions());
    }
	
	private void hideUnresolvedQuestions() {
		loadAllQuestions();
    }

    private VBox createAddQuestionPanel(Stage primaryStage,User user) {
    	VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        //create area to ask a question and submit question
        Label titleLabel = new Label("Ask a Question");
        TextArea questionInput = new TextArea();
        questionInput.setPromptText("Enter your question here...");
        Button submitButton = new Button("Submit Question");
        submitButton.setOnAction(e -> {
            String questionText = questionInput.getText().trim();
            if (!questionText.isEmpty()) {
                try {
                    dbHelper.addQuestion(dbHelper.getUserId(user.getUserName()), questionText); // Fixed user Id
                    questionInput.clear();
                    loadUnresolvedQuestions();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to add question.");
                }
            } else {
                showAlert("Input Error", "Please enter a question.");
            }
        });
        
        VBox panel1 = new VBox(10);
        panel.setPadding(new Insets(10));

        
        //create a button to return to user's home page
	    Button backButton = new Button("Back");
	    backButton.setOnAction(a -> {
			if(user.getRole().equals("reviewer")) {
				new ReviewerHomePage(dbHelper).show(primaryStage, user);
		    }else{
		    	new StudentHomePage(dbHelper).show(primaryStage, user);
		    }});

        panel.getChildren().addAll(titleLabel, questionInput, submitButton, backButton);
        return panel;
    }

    
    private VBox createAnswerPanel(User user) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label titleLabel = new Label("Add an Answer");
        TextArea answerInput = new TextArea();
        answerInput.setPromptText("Enter your answer here...");
        Button submitButton = new Button("Submit Answer");

        submitButton.setOnAction(e -> {
            Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                String answerText = answerInput.getText().trim();
                if (!answerText.isEmpty()) {
                    try {
                        dbHelper.addAnswer(selectedQuestion.getId(), dbHelper.getUserId(user.getUserName()), answerText); // Assuming userId = 1
                        answerInput.clear();
                        loadAnswersForQuestion(selectedQuestion.getId());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Database Error", "Failed to add answer.");
                    }
                } else {
                    showAlert("Input Error", "Please enter an answer.");
                }
            } else {
                showAlert("Selection Error", "Please select a question to answer.");
            }
        });

        panel.getChildren().addAll(titleLabel, answerInput, submitButton, new Label("Answers:"), answerListView);
        return panel;
    }
    
    private void setupButtons(VBox rightPanel) {
        // Delete Question Button
        Button deleteQuestionButton = new Button("Delete Question");
        deleteQuestionButton.setOnAction(e -> handleDeleteQuestion());

        // Update Question Button
        Button updateQuestionButton = new Button("Update Question");
        updateQuestionButton.setOnAction(e -> handleUpdateQuestion());

        // Delete Answer Button
        Button deleteAnswerButton = new Button("Delete Answer");
        deleteAnswerButton.setOnAction(e -> handleDeleteAnswer());

        // Update Answer Button
        Button updateAnswerButton = new Button("Update Answer");
        updateAnswerButton.setOnAction(e -> handleUpdateAnswer());

        // Add buttons to the UI layout
        VBox buttonPanel = new VBox(10, deleteQuestionButton, updateQuestionButton, deleteAnswerButton, updateAnswerButton);
        buttonPanel.setPadding(new Insets(10));

        // Add the button panel to the main layout
        rightPanel.getChildren().add(buttonPanel);
    }
    
    private void handleDeleteQuestion() {
        Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            try {
                dbHelper.deleteQuestion(selectedQuestion.getId());
                loadUnresolvedQuestions(); // Refresh the list of questions
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Database Error", "Failed to delete question.");
            }
        } else {
            showAlert("Selection Error", "Please select a question to delete.");
        }
    }
    
    private void handleUpdateQuestion() {
        Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            String newQuestionText = showUpdateDialog(selectedQuestion.getQuestionText());
            if (newQuestionText != null && !newQuestionText.isEmpty()) {
                try {
                    dbHelper.updateQuestion(selectedQuestion.getId(), newQuestionText);
                    loadUnresolvedQuestions(); // Refresh the list of questions
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to update question.");
                }
            }
        } else {
            showAlert("Selection Error", "Please select a question to update.");
        }
    }
    
    private void handleDeleteAnswer() {
        Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
        if (selectedAnswer != null) {
            try {
                dbHelper.deleteAnswer(selectedAnswer.getId());
                loadAnswersForQuestion(selectedAnswer.getQuestionId()); // Refresh the list of answers
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Database Error", "Failed to delete answer.");
            }
        } else {
            showAlert("Selection Error", "Please select an answer to delete.");
        }
    }

    private void handleUpdateAnswer() {
    	//pull Answer object from listView
        Answer selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
        if (selectedAnswer != null) {
        	//pop up new window to update answer, and input new answer into Answer object
            String newAnswerText = showUpdateDialog(selectedAnswer.getAnswerText());
            if (newAnswerText != null && !newAnswerText.isEmpty()) {
                try {
                    dbHelper.updateAnswer(selectedAnswer.getId(),newAnswerText);
                    loadAnswersForQuestion(selectedAnswer.getQuestionId()); // Refresh the list of answers
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "Failed to update answer.");
                }
            }
        } else {
            showAlert("Selection Error", "Please select an answer to update.");
        }
    }
    
    //Displays updated text
    private String showUpdateDialog(String currentText) {
        TextInputDialog dialog = new TextInputDialog(currentText);
        dialog.setTitle("Update");
        dialog.setHeaderText("Enter the new text:");
        dialog.setContentText("Text:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    //Gives useful error messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //Refreshes UI to show changes in questions
    private void loadUnresolvedQuestions() {
        questionsInView.clear();
        questionsInView.addAll(dbHelper.getUnresolvedQuestions());
    }
    private void loadAllQuestions() {
    	questionsInView.clear();
    	questionsInView.addAll(dbHelper.getAllQuestions());
    }
    //Shows search questions
    private void questionLookup(String question) {
    	questionsInView.clear();
    	questionsInView.addAll(dbHelper.searchQuestion(question));
    }
    
    //Refreshes UI to show changes in answers
    private void loadAnswersForQuestion(int questionId) {
        answerListView.setItems(dbHelper.getAnswersForQuestion(questionId));
    }

}