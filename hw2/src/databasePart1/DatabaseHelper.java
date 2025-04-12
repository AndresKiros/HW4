package databasePart1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import application.Answer;
import application.Message;
import application.Question;
import application.Review;
import application.RoleChangeRequest;
import application.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The DatabaseHelper class is responsible for managing the connection to the
 * database, performing operations such as user registration, login validation,
 * and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	private Connection connection = null;
	private Statement statement = null;
	// PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();// Create the necessary tables if they don't exist
			addRoles();
			addEmail();
			addQuestionId();
			addTrusted();
			// InvitationCodeTimer();
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, " + "password VARCHAR(255), " + "role VARCHAR(30)) ";
		statement.execute(userTable);

		//create sql table to hold all questions. contains userId of person who asked question, the question itself, and if the question is resolved
		String questionsTable = "CREATE TABLE IF NOT EXISTS Questions (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userId INT, " + "questionText TEXT, " + "isResolved BOOLEAN DEFAULT FALSE) ";
		statement.execute(questionsTable);

		//create sql table to hold all answers. has Id of original question and Id of user who proposed answer, has answer itself and if answer is accepted
		String answersTable = "CREATE TABLE IF NOT EXISTS Answers (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "questionId INT, " + "userId INT, " + "answerText TEXT, " + "isAccepted BOOLEAN DEFAULT FALSE, "
				+ "FOREIGN KEY (questionId) REFERENCES Questions(id), "
				+ "FOREIGN KEY (userId) REFERENCES cse360users(id)) ";
		statement.execute(answersTable);

		//create sql table to hold all messages. has sender and receiver Id's and Id of question that message is being sent about, has the message text and if message was read
		String messagesTable = "CREATE TABLE IF NOT EXISTS Messages (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "senderId INT, " + "receiverId INT, " + "messageText TEXT, " + "isRead BOOLEAN DEFAULT FALSE, "
				+ "FOREIGN KEY (senderId) REFERENCES cse360users(id), "
				+ "FOREIGN KEY (receiverId) REFERENCES cse360users(id)) ";
		statement.execute(messagesTable);
		
		//create sql table to hold all reviews. has Id of original answer, Id of reviewer, and review itself
		String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "answerId INT, " + "userId INT, " + "reviewText TEXT, "
				+ "FOREIGN KEY (answerId) REFERENCES Answers(id), "
				+ "FOREIGN KEY (userId) REFERENCES cse360users(id)) ";
		statement.execute(reviewsTable);
		
		// Create the invitation codes table, saves roles admin gives new user with code
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" + "code VARCHAR(10) PRIMARY KEY, "
				+ "isUsed BOOLEAN DEFAULT FALSE) ";
		statement.execute(invitationCodesTable);
		
		String roleChangesTable = "CREATE TABLE IF NOT EXISTS RoleChanges (" +
				"userName VARCHAR(255) UNIQUE, " +
				"newRole VARCHAR(30))";
		statement.execute(roleChangesTable);
		
		String staffTable = "CREATE TABLE IF NOT EXISTS StaffTable (" +"id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "StaffId VARCHAR(255), " + "comment VARCHAR(255)," + "genericId INT," + "createdAt VARCHAR(255)," + "isQuestion INT)";
		statement.execute(staffTable);
	}

	public void addComment(User user, String comment, int genericId, String createdAt, int isQuestion) throws SQLException {
	    String query = "INSERT INTO StaffTable (StaffId, comment, genericId, createdAt, isQuestion) VALUES (?, ?, ?, ?, ?)";
	    try(PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	pstmt.setString(1, user.getUserName());
	    	pstmt.setString(2, comment);
	        pstmt.setInt(3, genericId);
	        pstmt.setString(4, createdAt);
	        pstmt.setInt(5, isQuestion);
	    	pstmt.executeUpdate();
	    }
	}
	
	public String getComment(int genericId) throws SQLException {
	    String query = "SELECT comment FROM staffTable WHERE genericId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, genericId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getString("comment");
	            }
	        }
	    }
	    return null; 
	}
	
	// adds a new column in InvitationCodes table that stores roles given to new
	// users by admin
	public void addRoles() {
		String query = "ALTER TABLE InvitationCodes ADD COLUMN IF NOT EXISTS roles VARCHAR(40)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void roleChangeRequest(User user, String newRole) throws SQLException {
		String query = "INSERT INTO RoleChanges (userName, newRole) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, newRole);
			pstmt.executeUpdate();
		}
	}
	
	public ObservableList<RoleChangeRequest> viewRequest() {
		ObservableList<RoleChangeRequest> requests = FXCollections.observableArrayList();

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT userName, newRole FROM RoleChanges")) {

			while (rs.next()) {
				String username = rs.getString("userName");
				String role = rs.getString("newRole");
				requests.add(new RoleChangeRequest(username, role));
			}
			//testing
			int count = 0;
			while (rs.next()) {
				System.out.println(count);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return requests;
	}
	
	// Change user role
	public void changeRole(String userName, String newRole) throws SQLException {
	    // Checks if user exists
	    if (doesUserExist(userName)) {
	        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	            pstmt.setString(1, newRole);     
	            pstmt.setString(2, userName);    
	            int rows = pstmt.executeUpdate();
	            System.out.println("Updated rows: " + rows); // For testing purposes
	        }
	    } else {
	        System.out.println("User not found: " + userName);
	    }
	}

	//adds a new column in cse360users table to hold emails.
	public void addEmail() {
		String query = "ALTER TABLE cse360users ADD COLUMN IF NOT EXISTS email VARCHAR(255)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addQuestionId() {
		String query = "ALTER TABLE Messages ADD COLUMN IF NOT EXISTS questionId INT";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addTrusted() {
		String query = "ALTER TABLE cse360users ADD COLUMN IF NOT EXISTS isTrusted BOOLEAN DEFAULT FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void markTrusted(int userId) throws SQLException{
		String query = "UPDATE cse360users SET isTrusted = TRUE where id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.executeUpdate();
		}
	}
	
	public ObservableList<Review> filterTrusted() throws SQLException{
		ObservableList<Review> reviews = FXCollections.observableArrayList();
		String query = "SELECT id FROM cse360users WHERE isTrusted = TRUE";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				String query1 = "SELECT * FROM Reviews WHERE userId = ?";
				try (PreparedStatement pstmt = connection.prepareStatement(query)) {
					pstmt.setInt(1, rs.getInt("id"));
					ResultSet rs1 = pstmt.executeQuery(query1);
					while (rs1.next()) {
						Review review = new Review(rs1.getInt("id"), rs1.getInt("answerId"), rs1.getInt("userId"), rs1.getString("reviewText"));
						reviews.add(review);
					}
				}
			}
		}
		return reviews;
	}
	
	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role, email) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.setString(4, user.getEmail());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ? AND email = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.setString(4, user.getEmail());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {

			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// If the count is greater than 0, the user exists
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If an error occurs, assume user doesn't exist
	}
	
	//obtain userId based on userName
	public int getUserId(String userName) {
			String query = "SELECT id FROM cse360users WHERE userName = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, userName);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					return rs.getInt("id"); // Return the role if user exists
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0; // If no user exists or an error occurs
		}
		
		//will get userName based on user id
	public String getUserName(int id) {
			String query = "SELECT userName FROM cse360users WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, id);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					return rs.getString("userName"); // Return the role if user exists
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null; // If no user exists or an error occurs
		}

		// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
			String query = "SELECT role FROM cse360users WHERE userName = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, userName);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					return rs.getString("role"); // Return the role if user exists
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null; // If no user exists or an error occurs
		}

		// retrieve email from database
	public String getUserEmail(String userName) {
			String query = "SELECT email FROM cse360users WHERE userName = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, userName);
				ResultSet rs = pstmt.executeQuery();

				if (rs.next()) {
					return rs.getString("email"); // Return the role if user exists
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null; // If no user exists or an error occurs
		}	
		
	//function to remove users if they are not an admin
	public boolean removeUser(String userName) {
		String query = "DELETE FROM cse360users WHERE userName = ?";
		if (!getUserRole(userName).contains("admin")) {
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, userName);
				pstmt.executeUpdate();
				return true;

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			return false;
		}
		return false;
	}

	// Returns result list of all users
	public ObservableList<User> viewUsers() {

			// Array of users containing all the names
			ObservableList<User> userList = FXCollections.observableArrayList();

			String query = "SELECT userName FROM cse360users";
			try (Statement stmt = connection.createStatement()) {
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					String name = rs.getString("userName");
					String role = getUserRole(name);
					String email = getUserEmail(name);
					User x = new User(name, "x", role, email);
					userList.add(x);
				}
				;
				return userList;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return userList;
		}
	
	// Adds a new question to the database.
	public void addQuestion(int userId, String questionText) throws SQLException {
		String query = "INSERT INTO Questions (userId, questionText) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, questionText);
			pstmt.executeUpdate();
		}
	}

	// Marks a question as resolved.
	public void markQuestionAsResolved(int questionId) throws SQLException {
		String query = "UPDATE Questions SET isResolved = TRUE WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.executeUpdate();
		}
	}
	
	public int getUserIdFromQuestion(int questionId) {
		String query = "SELECT userId FROM Questions WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("userId"); // Return the userId if user exists
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0; // If no user exists or an error occurs
	}
	
	//will show all questions
	public ObservableList<Question> getAllQuestions() {
		ObservableList<Question> questions = FXCollections.observableArrayList();
		String query = "SELECT * FROM Questions ";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				Question question = new Question(rs.getInt("id"),
						rs.getInt("userId"),
						rs.getString("questionText"),
						rs.getBoolean("isResolved"));
				questions.add(question);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}
	
	//will show all questions user has made
	public ObservableList<Question> getMyQuestions(int userId) {
		ObservableList<Question> questions = FXCollections.observableArrayList();
		String query = "SELECT * FROM Questions WHERE userId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Question question = new Question(rs.getInt("id"), userId, rs.getString("questionText"),
						rs.getBoolean("isResolved"));
				questions.add(question);
			}
		} catch (SQLException e) {
			e.printStackTrace();	
		}
			return questions;
	}
	
	//will will obtain all questions from other users, so user can send a message
	public ObservableList<Question> getOtherQuestions(int userId) {
			ObservableList<Question> questions = FXCollections.observableArrayList();
			String query = "SELECT * FROM Questions WHERE userId != ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, userId);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					Question question = new Question(rs.getInt("id"), userId, rs.getString("questionText"),
							rs.getBoolean("isResolved"));
					questions.add(question);
				}
			} catch (SQLException e) {
				e.printStackTrace();	
			}
				return questions;
		}
	
	// Retrieves all unresolved questions.
	public ObservableList<Question> getUnresolvedQuestions() {
		ObservableList<Question> questions = FXCollections.observableArrayList();
		//String query = "SELECT * FROM Questions WHERE isResolved = FALSE";
		String query = "SELECT q.* FROM Questions q " +
                "LEFT JOIN Answers a ON q.id = a.questionId " +
                "WHERE q.isResolved = FALSE AND a.id IS NULL";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				Question question = new Question(rs.getInt("id"), rs.getInt("userId"), rs.getString("questionText"),
						rs.getBoolean("isResolved"));
				questions.add(question);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return questions;
	}

	// Searches for question based on text input
	public ObservableList<Question> searchQuestion(String input) {
		// Final matching questions
		ObservableList<Question> questions = FXCollections.observableArrayList();
		// Temporary
		ArrayList<Question> allQuestions = new ArrayList<>();
		// Input tokenized into words
		String[] words = input.split(" ");
		String query = "SELECT * FROM Questions";
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Question question = new Question(rs.getInt("id"), rs.getInt("userId"), rs.getString("questionText"),
						rs.getBoolean("isResolved"));
				allQuestions.add(question);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	
		for (Question q : allQuestions) { // Iterates through questions with q being iterator.
			String[] qWords = q.getQuestionText().split(" "); // Places each of the question's words into an array.
			for (String inputW : words) { // Iterates through word array of what the user has inputed.
				for (String questionW : qWords) { // For every question words array...
					if (inputW.equalsIgnoreCase(questionW)) { // Checks if they are the same word.
						questions.add(q);
						break;
					}
				}
				if(questions.contains(q))break;
			}
		}
		return questions;
	}
	
	// Delete a question after its been added to the database
	public void deleteQuestion(int questionId) throws SQLException {
			String query = "DELETE FROM Questions WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, questionId);
				pstmt.executeUpdate();
			}
		}
		
		// Update a question after it has been added to the database
	public void updateQuestion(int questionId, String newQuestionText) throws SQLException {
			String query = "UPDATE Questions SET questionText = ? WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, newQuestionText);
				pstmt.setInt(2, questionId);
				pstmt.executeUpdate();
			}
		}	
		
		// Adds a new answer to the database.
	public void addAnswer(int questionId, int userId, String answerText) throws SQLException {
			// Check if the userId exists in cse360users
			String userQuery = "SELECT COUNT(*) FROM cse360users WHERE id = ?";
			try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
				userStmt.setInt(1, userId);
				ResultSet userRs = userStmt.executeQuery();
				if (userRs.next() && userRs.getInt(1) == 0) {
					throw new SQLException("Invalid userId: " + userId);
				}
			}
			
			// Check if the questionId exists in Questions
			String questionQuery = "SELECT COUNT(*) FROM Questions WHERE id = ?";
			try (PreparedStatement questionStmt = connection.prepareStatement(questionQuery)) {
				questionStmt.setInt(1, questionId);
				ResultSet questionRs = questionStmt.executeQuery();
				if (questionRs.next() && questionRs.getInt(1) == 0) {
					throw new SQLException("Invalid questionId: " + questionId);
				}
			}
			String query = "INSERT INTO Answers (questionId, userId, answerText) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, questionId);
				pstmt.setInt(2, userId);
				pstmt.setString(3, answerText);
				pstmt.executeUpdate();
			}
		}

	// Retrieves all answers for a specific question.
	public ObservableList<Answer> getAnswersForQuestion(int questionId) {
		ObservableList<Answer> answers = FXCollections.observableArrayList();
		String query = "SELECT * FROM Answers WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Answer answer = new Answer(rs.getInt("id"), rs.getInt("questionId"), rs.getInt("userId"),
						rs.getString("answerText"), rs.getBoolean("isAccepted"));
				answers.add(answer);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return answers;
	}
	
	// Delete an answer after its been added to the database
	public void deleteAnswer(int answerId) throws SQLException {
			String query = "DELETE FROM Answers WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, answerId);
				pstmt.executeUpdate();
			}
		}

		// Update an answer after it has been added to the database
	public void updateAnswer(int answerId, String newAnswerText) throws SQLException {
			String query = "UPDATE Answers SET answerText = ? WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, newAnswerText);
				pstmt.setInt(2, answerId);
				pstmt.executeUpdate();
			}
		}

		//Marks an answer as accepted
	public void markAnswersAccepted(int answerId) throws SQLException {
			String query = "UPDATE Answers SET isAccepted = TRUE WHERE id = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, answerId);
				pstmt.executeUpdate();
			}
		}	
		
	// Sends a private message from one user to another.
	public void sendMessage(int senderId, int receiverId, int questionId, String messageText) throws SQLException {
		String query = "INSERT INTO Messages (senderId, receiverId, questionId, messageText) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, senderId);
			pstmt.setInt(2, receiverId);
			pstmt.setInt(3, questionId);
			pstmt.setString(4, messageText);
			pstmt.executeUpdate();
		}
	}

	// Retrieves all unread messages for a specific user.
	public ObservableList<Message> getUnreadMessages(int userId, int questionId) {
		ObservableList<Message> messages = FXCollections.observableArrayList();
		String query = "SELECT * FROM Messages WHERE questionId = ? AND receiverId = ? AND isRead = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.setInt(2, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Message message = new Message(rs.getInt("id"), rs.getInt("senderId"), rs.getInt("receiverId"),
						rs.getInt("questionId"), rs.getString("messageText"), rs.getBoolean("isRead"));
				messages.add(message);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	//will mark messages as read in database
	public void markAsRead(int messageId) throws SQLException {
		String query = "UPDATE Messages SET isRead = TRUE WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, messageId);
			pstmt.executeUpdate();
		}
	}
	
	//will return all messages based on questionId so users can see all messages people have sent them
	public ObservableList<Message> getMessagesForQuestion(int senderId, int questionId) {
		ObservableList<Message> messages = FXCollections.observableArrayList();
		//obtain all messages about a question that user wrote or received (only for use when send messages about other peoples questions)
		String query = "SELECT * FROM Messages WHERE questionId = ? AND (senderId = ? OR receiverId = ?) ";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.setInt(2, senderId);
			pstmt.setInt(3, senderId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Message newMessage = new Message(rs.getInt("id"), rs.getInt("senderId"), rs.getInt("receiverId"),
						rs.getInt("questionId"), rs.getString("messageText"), rs.getBoolean("isRead"));
				messages.add(newMessage);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	//will delete message in database based on message Id
	public void deleteMessage(int messageId) throws SQLException {
		String query = "DELETE FROM Messages WHERE id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, messageId);
			pstmt.executeUpdate();
		}
	}
	
	//will update Message in database
	public void updateMessage(int messageId, String newMessageText) throws SQLException {
		String query = "UPDATE Messages SET messageText = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newMessageText);
			pstmt.setInt(2, messageId);
			pstmt.executeUpdate();
		}
	}

	public void addReview(int answerId, int userId, String reviewText) throws SQLException {
		String query = "INSERT into Reviews (answerid, userid, reviewText) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			pstmt.setInt(2, userId);
			pstmt.setString(3, reviewText);
			pstmt.executeUpdate();
		}
	}
	
	public void updateReview(int reviewId, String newReviewText) throws SQLException {
		String query = "UPDATE Reviews SET reviewText = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newReviewText);
			pstmt.setInt(2, reviewId);
			pstmt.executeUpdate();
		}
	}
	
	public void deleteReview(int reviewId) throws SQLException {
		String query = "DELETE FROM Reviews WHERE id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, reviewId);
			pstmt.executeUpdate();
		}
	}
	
	public ObservableList<Review> getMyReviews(int userId) {
		ObservableList<Review> reviews = FXCollections.observableArrayList();
		String query = "SELECT * FROM Reviews WHERE userId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Review review = new Review(rs.getInt("id"), rs.getInt("answerid"), userId, rs.getString("reviewText"));
				reviews.add(review);
			}
		} catch (SQLException e) {
			e.printStackTrace();	
		}
			return reviews;
	}
	
	// Retrieves all answers for a specific question.
		public ObservableList<Review> getReviewsForAnswer(int answerId) {
			ObservableList<Review> reviews = FXCollections.observableArrayList();
			String query = "SELECT * FROM Reviews WHERE answerId = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setInt(1, answerId);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					Review review = new Review(rs.getInt("id"), answerId, rs.getInt("userId"),
							rs.getString("reviewText"));
					reviews.add(review);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return reviews;
		}
		
	
	// Get messages for a specific review
	public ObservableList<Message> getMessagesForReview(int reviewId, int userId) {
	    ObservableList<Message> messages = FXCollections.observableArrayList();
	    // First get the review to get the answerId
	    String reviewQuery = "SELECT answerId FROM Reviews WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(reviewQuery)) {
	        pstmt.setInt(1, reviewId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int answerId = rs.getInt("answerId");
	            // Now get messages related to this answerId (using questionId field)
	            String messageQuery = "SELECT * FROM Messages WHERE questionId = ? AND (senderId = ? OR receiverId = ?)";
	            try (PreparedStatement msgStmt = connection.prepareStatement(messageQuery)) {
	                msgStmt.setInt(1, answerId);
	                msgStmt.setInt(2, userId);
	                msgStmt.setInt(3, userId);
	                ResultSet msgRs = msgStmt.executeQuery();
	                while (msgRs.next()) {
	                    Message message = new Message(
	                        msgRs.getInt("id"),
	                        msgRs.getInt("senderId"),
	                        msgRs.getInt("receiverId"),
	                        msgRs.getInt("questionId"),
	                        msgRs.getString("messageText"),
	                        msgRs.getBoolean("isRead")
	                    );
	                    messages.add(message);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return messages;
	}
	public ObservableList<Review> getAllReviews() throws SQLException {
	    ObservableList<Review> reviews = FXCollections.observableArrayList();
	    String query = "SELECT * FROM Reviews";
	    
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            Review review = new Review(
	                rs.getInt("id"),
	                rs.getInt("answerId"), 
	                rs.getInt("userId"),
	                rs.getString("reviewText")
	            );
	            reviews.add(review);
	        }
	    }
	    return reviews;
	}
	
	// Send message about a review
	public void sendReviewMessage(int senderId, int receiverId, int reviewId, String messageText) throws SQLException {
	    // First get the answerId from the review
	    String reviewQuery = "SELECT answerId FROM Reviews WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(reviewQuery)) {
	        pstmt.setInt(1, reviewId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int answerId = rs.getInt("answerId");
	            // Now send the message using answerId as questionId
	            sendMessage(senderId, receiverId, answerId, messageText);
	        }
	    }
	}
	
	// obtain roles given by admin from the database based on invitation code
	public String getInviteRoles(String code) {
		String query = "SELECT roles FROM InvitationCodes WHERE code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getString("roles"); // Return the role if user exists
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // If no user exists or an error occurs
	}

	// Generates a new invitation code and inserts it into the database, as well as roles admin permit's user to have.
	public String generateInvitationCode(String role) {
		String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
		String query = "INSERT INTO InvitationCodes (code, roles) VALUES (?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.setString(2, role);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return code;
	}

	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
		String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				// Mark the code as used
				markInvitationCodeAsUsed(code);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
		String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// will delete all invitation codes that are not used after 48 hours
	private void InvitationCodeTimer() {
		String query = "DELETE FROM InvitationCodes WHERE code < '48:00:00'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

}
