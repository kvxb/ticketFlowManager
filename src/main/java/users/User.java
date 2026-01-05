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


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public Role getRole() {
		return role;
	}


	public void setRole(Role role) {
		this.role = role;
	}
}
