package users;

public class User {
    protected String username;
    private String email;

    public enum Role {
        REPORTER,
        DEVELOPER,
        MANAGER
    }

    private Role role;

    public User(final String username, final String email, final String role) {
        this.username = username;
        this.email = email;
        this.role = Role.valueOf(role);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
