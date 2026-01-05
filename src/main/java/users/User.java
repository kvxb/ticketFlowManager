package users;

public class User {
    private String username;
    private String email;

    public enum Role {
        REPORTER,
        DEVELOPER,
        MANAGER
    }

    private Role role;


    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = Role.valueOf(role);
    }
}
