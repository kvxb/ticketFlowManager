package users;

public class User {
    private String username;
    private String email;

    public enum role {
        REPORTER,
        DEVELOPER,
        MANAGER
    }
}
