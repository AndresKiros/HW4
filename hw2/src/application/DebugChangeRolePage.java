package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;  // Use this!


import java.sql.SQLException;

import javax.swing.*;

/**
 * This page displays a simple welcome message for the user.
 */

public class DebugChangeRolePage {

	private final DatabaseHelper databaseHelper;

	public DebugChangeRolePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage, User user) {
		VBox layout = new VBox();
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		// Label to display Hello Staff
		Label userLabel = new Label("Change your role here!");
		userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		Scene userScene = new Scene(layout, 800, 400);

		// Set the scene to primary stage
		primaryStage.setScene(userScene);
		primaryStage.setTitle(" !For DEBUG purposes! Change Role Page");
		
		//Change role
        TextField textField = new TextField();
        textField.getText();
        Button changeRole = new Button("Confirm");
        changeRole.setOnAction(a -> {
        	try {
				databaseHelper.changeRole(user.getUserName(), textField.getText());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
		
        //create a button to return to user's home page
	    Button backButton = new Button("Back");
	    backButton.setOnAction(a -> {
			if(user.getRole().equals("reviewer")) {
				new ReviewerHomePage(databaseHelper).show(primaryStage, user);
				}
			if(user.getRole().equals("staff")) {
					new StaffHomePage(databaseHelper).show(primaryStage, user);
			if(user.getRole().equals("admin")) {	//This function won't work because admin does not pass through the user object.
				new AdminHomePage(databaseHelper).show(primaryStage);
				}
			if(user.getRole().equals("instructor")) {
				new ReviewerHomePage(databaseHelper).show(primaryStage, user);
				}
			
		    }else{
		    	new StudentHomePage(databaseHelper).show(primaryStage, user);
		    }});

	    layout.getChildren().addAll(userLabel,textField,changeRole, backButton);

    }
	
    	
}
