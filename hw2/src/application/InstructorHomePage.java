package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * This page displays a simple welcome message for the user.
 */

public class InstructorHomePage {
	
	private final DatabaseHelper databaseHelper;

	 public InstructorHomePage(DatabaseHelper databaseHelper) {
	    	this.databaseHelper = databaseHelper;
		}

	public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to greet Instructor
	    Label userLabel = new Label("Hello, Instructor!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Instructor Page");
	    
	    // Takes you to page where you can change the role of a student
	    Button notifications = new Button("Notifications");
	    notifications.setOnAction(a -> showReviewerRequests());
	    
	    // Takes you to changeRole page (FOR DEBUG PURPOSES) 
	    Button changeRoleButton = new Button("Change Role");
	    changeRoleButton.setOnAction(a -> {
	    	new DebugChangeRolePage(databaseHelper).show(primaryStage, user);
	    });
	    
	    // Takes you to login page
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(a -> {
	    	new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    layout.getChildren().addAll(userLabel, notifications,logoutButton);
 	
    }
	
    private void showAlert(String title, String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //This will be changed to view all user request
    public void showReviewerRequests() {
        // Change to get list of users that requested change
        ObservableList<RoleChangeRequest> users = databaseHelper.viewRequest();

        // Create TableView
        TableView<RoleChangeRequest> tableView = new TableView<>();
        tableView.setItems(users); // Set data

        // Define columns
        TableColumn<RoleChangeRequest, String> nameColumn = new TableColumn<>("User Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName")); 

        TableColumn<RoleChangeRequest, String> roleColumn = new TableColumn<>("Requested Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("newRole")); 
        

        // Add columns to the table
        tableView.getColumns().addAll(nameColumn, roleColumn);

        // Layout
        VBox vbox = new VBox(10, tableView);
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Stage 
        Stage tableStage = new Stage();
        tableStage.setTitle("Request List");
        tableStage.setScene(new Scene(vbox, 400, 300));
        tableStage.show();
        
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                RoleChangeRequest selectedRequest = tableView.getSelectionModel().getSelectedItem();
                try {
					databaseHelper.changeRole(selectedRequest.getUserName(), selectedRequest.getNewRole());
					showAlert("Action Successful", "User " + selectedRequest.getUserName() + " has been given the role of " + selectedRequest.getNewRole(), null);
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        });
        
        
        
    }
}