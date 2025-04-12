package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class removeUserPage {
private final DatabaseHelper databaseHelper;
	
    public removeUserPage(DatabaseHelper databaseHelper) {
    	this.databaseHelper = databaseHelper;
	}
	
	
    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    
	    Scene adminScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Remove Users");
	    
	  //sets label for instructions for textField
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    Label users = new Label("Select Users to Remove:");
	    
	    //creates textField to input user to remove
	    TextField userField = new TextField();
	    userField.setPromptText("Enter User");
        userField.setMaxWidth(250);
        
        //creates label for error messages
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
      //Prints out users
	    AdminHomePage adminhome = new AdminHomePage(databaseHelper);
	    adminhome.showUserTable();
	    
        //button to confirm removal
        Button removeButton = new Button("Remove from Class");
        
        removeButton.setOnAction(a -> {
        	//obtains userName to delete
        	String userName = userField.getText();
        	if (databaseHelper.doesUserExist(userName) == false) {
        		//if user does not exist error messages prints out
        		errorLabel.setText("User does not exist");
        		
        	} else {
        		//takes you to new stage to confirm to delete user
        		 Button yes = new Button("Yes");
                 Button no = new Button("No");
                 
        		VBox vbox = new VBox(10, yes, no);
                vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");
                
                // Stage 
                Stage confirm = new Stage();
                confirm.setTitle("Are you Sure?");
                confirm.setScene(new Scene(vbox, 400, 300));
                confirm.show();
                //if yes, will delete the user and print out to console, if error occurs prints out to console. will close stage after
               yes.setOnAction(b -> {
            	   if(databaseHelper.removeUser(userName) == true){
               			adminhome.showUserTable();
               			System.out.print("User successfully removed");
            	   } else {
            		   System.out.print("Error removing User");
            	   }
            	   confirm.close();
               });
                //if no will close stage
               no.setOnAction(b -> {
            	   confirm.close();
               });
                
        	}
        });
	    
	    layout.getChildren().addAll(users, userField, removeButton, errorLabel);
    }
}
