package users;

/**
 * Represents a Manager user in the system.
 * Managers oversee developers and milestones.
 */
public final class Manager extends User {
    private String hireDate;
    private String[] subordinates;

    /**
     * Creates a new Manager instance.
     *
     * @param username     The unique username.
     * @param email        The user's email address.
     * @param role         The user's role (MANAGER).
     * @param hireDate     The date the manager was hired.
     * @param subordinates Array of usernames of developers managed by this user.
     */
    public Manager(final String username, final String email, final String role,
                   final String hireDate, final String[] subordinates) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }

    /**
     * Gets the hire date.
     *
     * @return The hire date string.
     */
    public String getHireDate() {
        return hireDate;
    }

    /**
     * Sets the hire date.
     *
     * @param hireDate The new hire date.
     */
    public void setHireDate(final String hireDate) {
        this.hireDate = hireDate;
    }

    /**
     * Gets the list of subordinate usernames.
     *
     * @return Array of usernames.
     */
    public String[] getSubordinates() {
        return subordinates;
    }

    /**
     * Sets the list of subordinate usernames.
     *
     * @param subordinates Array of usernames.
     */
    public void setSubordinates(final String[] subordinates) {
        this.subordinates = subordinates;
    }
}
