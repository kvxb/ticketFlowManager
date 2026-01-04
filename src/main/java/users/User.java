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

}
