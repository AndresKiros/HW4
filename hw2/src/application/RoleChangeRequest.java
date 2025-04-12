package application;

public class RoleChangeRequest {
    private String userName;
    private String newRole;

    public RoleChangeRequest(String userName, String newRole) {
        this.userName = userName;
        this.newRole = newRole;
    }

    public String getUserName() {
        return userName;
    }

    public String getNewRole() {
        return newRole;
    }
}