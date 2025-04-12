package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName, email, and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Admin email");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button setupButton = new Button("Setup");
        
        //presents error messages for username
        Label userErrorLabel = new Label();
        userErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //presents error messages for password
        Label passErrorLabel = new Label();
        passErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //presents error messages for email
        Label emailErrorLabel = new Label();
        emailErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        
        
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            
           
            try {
            	//stays on login page until both username and password are created correctly
            	if ((PasswordEvaluator.evaluatePassword(password) != "") 
            			||(UserNameRecognizer.checkForValidUserName(userName) != "") 
            			||(EmailEvaluation.checkForASUEmail(email) != "")) {
            		passErrorLabel.setText("Password needs: " + PasswordEvaluator.evaluatePassword(password));
        			emailErrorLabel.setText(EmailEvaluation.checkForASUEmail(email));
        			userErrorLabel.setText("UserName: " + UserNameRecognizer.checkForValidUserName(userName));
            	} else {
            	
            	// Create a new User object with admin role and register in the database
            	User user=new User(userName, password, "admin", email);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Welcome Login Page
                new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, userNameField, emailField, passwordField, setupButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userErrorLabel, passErrorLabel, emailErrorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
