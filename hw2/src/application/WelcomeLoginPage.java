package application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!, which home page would you like to go to?");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    layout.getChildren().addAll(welcomeLabel);
	    
	    //pulls roles of user from database
	    String role =user.getRole();
	    
	    String regex = "[,\\s]";
	    String[] roles = role.split(regex);
	    //will check the roles of the user and create buttons for them to navigate to the home page that they want
	    for (String s: roles) {
	    	if(s.equalsIgnoreCase("student")) {
	    		Button student = new Button("Student");
	    		student.setOnAction(a -> {
	    			StudentHomePage studenthome = new StudentHomePage(databaseHelper);
	    			studenthome.show(primaryStage, user);
	    		});
	    		layout.getChildren().add(student);
	    	}
			if(s.equalsIgnoreCase("reviewer")) {
				Button reviewer = new Button("Reviewer");
	            reviewer.setOnAction(a -> {
	              ReviewerHomePage reviewerhome = new ReviewerHomePage(databaseHelper);
	              reviewerhome.show(primaryStage, user);
	            });
	            layout.getChildren().add(reviewer);	
			}
			if(s.equalsIgnoreCase("instructor")) {
				Button instructor = new Button("Instructor");
	            instructor.setOnAction(a -> {
	              InstructorHomePage instructorhome = new InstructorHomePage(databaseHelper);
	              instructorhome.show(primaryStage, user);
	            });
	            layout.getChildren().add(instructor);	
			}
			if(s.equalsIgnoreCase("staff")) {
				Button staff = new Button("Staff");
	            staff.setOnAction(a -> {
	              StaffHomePage staffhome = new StaffHomePage(databaseHelper);
	              staffhome.show(primaryStage, user);
	            });
	            layout.getChildren().add(staff);	 
			}
			//if user is admin they have access to all user home pages
			if(s.equalsIgnoreCase("admin")) {
				Button admin = new Button("Admin");
	            admin.setOnAction(a -> {

	              //new AdminHomePage().show(databaseHelper, primaryStage);
	            	AdminHomePage adminHome = new AdminHomePage(databaseHelper);
	            	adminHome.show(primaryStage);
	            });
	            
	            Button student = new Button("Student");
	    		student.setOnAction(a -> {
	    			StudentHomePage studenthome = new StudentHomePage(databaseHelper);
	    			studenthome.show(primaryStage, user);
	    		});
	    		
	    		Button reviewer = new Button("Reviewer");
	    		reviewer.setOnAction(a -> {
	    			new ReviewerHomePage(databaseHelper).show(primaryStage, user);
	    		});
	    		
	    		Button instructor = new Button("Instructor");
	    		instructor.setOnAction(a -> {
	    			new InstructorHomePage(databaseHelper).show(primaryStage, user);
	    		});
	    		
	            Button staff = new Button("Staff");
	            staff.setOnAction(a -> {
	            	new StaffHomePage(databaseHelper).show(primaryStage, user);
	            });
	    		
	            layout.getChildren().addAll(admin, student, reviewer, instructor, staff);
	            

	           
			}
	    }
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });

	    layout.getChildren().addAll(quitButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
	    primaryStage.show();
    }
}
