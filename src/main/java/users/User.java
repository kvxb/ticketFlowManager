package users;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
