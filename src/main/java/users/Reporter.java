package users;

/**
 * Represents a Reporter user in the system.
 * Reporters can create tickets and view the ones they reported.
 */
public final class Reporter extends User {

    /**
     * Creates a new Reporter instance.
     *
     * @param username The unique username.
     * @param email    The user's email address.
     * @param role     The user's role (REPORTER).
     */
    public Reporter(final String username, final String email, final String role) {
        super(username, email, role);
    }
}
