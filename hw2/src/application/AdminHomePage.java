package application;
import java.sql.*;

import java.util.ArrayList;


import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	

	private final DatabaseHelper databaseHelper;
	
    public AdminHomePage(DatabaseHelper databaseHelper) {
    	this.databaseHelper = databaseHelper;
	}
	
	
    public void show(Stage primaryStage) {
    	VBox layout = new VBox(10);
    	
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display the welcome message for the admin
	    Label adminLabel = new Label("Hello, Admin!");
	    
	    adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Scene adminScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
	    
	    
	    //Prints out users
	    Button viewUsers = new Button("View users");
	    viewUsers.setOnAction(a -> showUserTable());
	
	    
	    //Takes you to invitation page
	    Button invitation = new Button("Invite");
	    invitation.setOnAction(a -> {
	    	new InvitationPage().show(databaseHelper, primaryStage);

	    });
	    
	    //Takes you to login page
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> {
	    	new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    
	    //takes you to remove user page
	    Button removeButton = new Button("Remove User");
	    removeButton.setOnAction(a -> {
	    	new removeUserPage(databaseHelper).show(primaryStage);
	    });
	    
	    layout.getChildren().addAll(adminLabel, invitation, viewUsers, removeButton, logoutButton);
    }
    
    //function to view all users in server
    public void showUserTable() {
        // Fetch user data
        ObservableList<User> users = databaseHelper.viewUsers();

        // Create TableView
        TableView<User> tableView = new TableView<>();
        tableView.setItems(users); // Set data

        // Define columns
        TableColumn<User, String> nameColumn = new TableColumn<>("User Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName")); 

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role")); 
        
        TableColumn<User, String> emailColumn = new TableColumn<>("Email Address");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); 

        // Add columns to the table
        tableView.getColumns().addAll(nameColumn, roleColumn, emailColumn);

        // Layout
        VBox vbox = new VBox(10, tableView);
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Stage 
        Stage tableStage = new Stage();
        tableStage.setTitle("User List");
        tableStage.setScene(new Scene(vbox, 400, 300));
        tableStage.show();
    }
}
