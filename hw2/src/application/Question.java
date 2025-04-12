package application;

public class Question {
    private int id;
    private int userId;
    private String questionText;
    private boolean isResolved;

    // Constructor
    public Question(int id, int userId, String questionText, boolean isResolved) {
        this.id = id;
        this.userId = userId;
        this.questionText = questionText;
        this.isResolved = isResolved;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", userId=" + userId +
                ", questionText='" + questionText + '\'' +
                ", isResolved=" + isResolved +
                '}';
    }
}