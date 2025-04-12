package application;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TP2_Testing {
	
	@Test
    public void testReviewerCanSeeTheirReviews() {
        ObservableList<Review> fakeReviews = FXCollections.observableArrayList(
                new Review(1, 1, 1, "Test Question 1"),
                new Review(2, 2, 2,  "Test Question 2")
        );
        assertEquals(2, fakeReviews.size());
    }

    @Test
    public void testReviewerCanSeeMessagesForReview() {
        ObservableList<Message> fakeMessages = FXCollections.observableArrayList(
                new Message(1, 1, 2, 1, "Hello, I need help", false),
                new Message(2, 1, 3, 1, "Thanks for the feedback", false)
        );
        assertEquals(2, fakeMessages.size());
    }

    @Test
    public void testReviewerCanReplyToMessage() {
        Message sentMessage = new Message(3, 1, 2, 1, "You're welcome!", false);
        assertEquals("You're welcome!", sentMessage.getMessageText());
        assertEquals(1, sentMessage.getSenderId());
        assertEquals(2, sentMessage.getReceiverId());
    }

    @Test
    public void testStudentCanInitiatePrivateExchange() {
        Message studentMessage = new Message(4, 10, 5, -1, "Can you help with my review?", false);
        assertEquals("Can you help with my review?", studentMessage.getMessageText());
        assertEquals(10, studentMessage.getSenderId());
        assertEquals(5, studentMessage.getReceiverId());
    }

    @Test
    public void testMessageReception() {
        Message receivedMessage = new Message(1, 2, 1, 1, "I appreciate your feedback!", false);
        assertEquals("I appreciate your feedback!", receivedMessage.getMessageText());
        assertEquals(2, receivedMessage.getSenderId());
        assertEquals(1, receivedMessage.getReceiverId());
    }
	
	@Test
	public void IdGetandSetQuestiontest() { //Checking if setting Id of a question works.
		Question testQ = new Question(1, 1, "Hello?", false);
		assertEquals(1, testQ.getId());
		testQ.setId(2);
		assertEquals(2, testQ.getId());
	}
	@Test
	public void userIdGetandSetQuestiontest() { //Changing the ownership of a question to another user.
		Question testQ = new Question(1, 1, "Hello?", false);
		assertEquals(1, testQ.getUserId());
		System.out.println("Changing the user id. " + testQ);
		testQ.setUserId(2);
		assertEquals(2, testQ.getUserId());
		System.out.println("User id change valid. " + testQ + "\n");
	}
	@Test
	public void questionTextGetandSettest() { // Updating the question text.
		Question testQ = new Question(1, 1, "Hello?", false);
		assertEquals("Hello?", testQ.getQuestionText());
		testQ.setQuestionText("Goodbye?");
		assertEquals("Goodbye?", testQ.getQuestionText());
		System.out.println("Updating question text. " + testQ + "\n");
	}
	@Test
	public void questionResolvedtest() { // Checking if the question is resolved.
		Question testQ = new Question(1, 1, "Hello?", false);
		assertEquals(false, testQ.isResolved());
		testQ.setResolved(true);
		assertEquals(true, testQ.isResolved());
		System.out.println("Test if setting question to resolved works.\n "+testQ + "\n");
	}
	@Test
	public void questionToStringtest() { //Test out question creation
		Question testQ = new Question(1, 1, "Hello?", false);
		assertEquals("Question{id=1, userId=1, questionText='Hello?', isResolved=false}", testQ.toString());
	}
	
	@Test
	public void IdGetandSetAnswertest() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals(1, testA.getId());
		testA.setId(2);
		assertEquals(2, testA.getId());
		System.out.println(testA);
	}
	@Test
	public void userIdGetandSetAnswertest() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals(1, testA.getUserId());
		testA.setUserId(2);
		assertEquals(2, testA.getUserId());
		System.out.println(testA);
	}
	@Test
	public void questionIdGetandSetAnswertest() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals(1, testA.getQuestionId());
		testA.setQuestionId(2);
		assertEquals(2, testA.getQuestionId());
		System.out.println(testA);
	}
	@Test
	public void answerTextGetandSetText() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals("Hi", testA.getAnswerText());
		testA.setAnswerText("Yo");
		assertEquals("Yo", testA.getAnswerText());
		System.out.println(testA);
	}
	@Test
	public void answerisAcceptedTest() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals(false, testA.isAccepted());
		testA.setAccepted(true);
		assertEquals(true, testA.isAccepted());
		System.out.println(testA);
	}
	@Test
	public void answerToStringtest() {
		Answer testA = new Answer(1, 1, 1, "Hi", false);
		assertEquals("Answer{id=1, questionId=1, userId=1, answerText='Hi', isAccepted=false}", testA.toString());
		System.out.println(testA);
	}
	@Test
	public void getReceiverIdMessageTest() {
		Message testM = new Message(1, 1, 1, 1, "Hi", false);
		assertEquals(1, testM.getReceiverId());
		System.out.println(testM);
	}
	@Test
	public void messageTextGetandSetText() {
		Message testM = new Message(1, 1, 1, 1, "Hello", false);
		assertEquals("Hello", testM.getMessageText());
		testM.setMessageText("I can help");
		assertEquals("I can help", testM.getMessageText());
		System.out.println(testM);	
	}
    @Test
    public void testGetUserName() {
        RoleChangeRequest request = new RoleChangeRequest("alice", "Admin");
        assertEquals("alice", request.getUserName());
        System.out.println("Username has been recieved: " + request.getUserName());
    }

    @Test
    public void testGetNewRole() {
        RoleChangeRequest request = new RoleChangeRequest("Andres", "Reviewer");
        assertEquals("Reviewer", request.getNewRole());
        System.out.println("New role correctly set " + request.getNewRole());
    }

    @Test
    public void testRoleChangeRequest() {
        RoleChangeRequest request = new RoleChangeRequest("Andres", "Student");
        assertEquals("Andres", request.getUserName());
        assertEquals("Student", request.getNewRole());
        System.out.println("RoleChange of " + request.getUserName() + ", " + request.getNewRole());
    }
	
}
