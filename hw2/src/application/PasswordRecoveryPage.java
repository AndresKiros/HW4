package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * This page displays the user three security questions to generate a temporary password that can only be used once.
 */

public class PasswordRecoveryPage {
	
	private final DatabaseHelper databaseHelper;

	// constructor
    public PasswordRecoveryPage(DatabaseHelper databaseHelper) {
    	this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label userLabel = new Label("Password Recovery:");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    layout.getChildren().add(userLabel);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Password Recovery Page");
	    
	    // Body of the page:
	    TextField input1= new TextField();
	    input1.setPromptText("Name the street you grew up in: ");
	    input1.setMaxWidth(250);
	    new Label("\n");
	    
	    TextField input2= new TextField();
	    input2.setPromptText("What was the name of your first pet: ");
	    input2.setMaxWidth(250);
	    new Label("\n");
	    
	    TextField input3= new TextField();
	    input2.setPromptText("Name the city you were born in");
	    input2.setMaxWidth(250);
    	
    }
}