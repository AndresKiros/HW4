package application;

public class Answer {
    private int id;
    private int questionId;
    private int userId;
    private String answerText;
    private boolean isAccepted;

    // Constructor
    public Answer(int id, int questionId, int userId, String answerText, boolean isAccepted) {
        this.id = id;
        this.questionId = questionId;
        this.userId = userId;
        this.answerText = answerText;
        this.isAccepted = isAccepted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", userId=" + userId +
                ", answerText='" + answerText + '\'' +
                ", isAccepted=" + isAccepted +
                '}';
    }
}