package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import milestones.Milestone;

/**
 * Handler for the command : assignTicket.
 * Implements the Chain of Responsibility pattern for validating developer
 * assignments.
 */
public abstract class DeveloperValidationHandler {
    private DeveloperValidationHandler next;

    /**
     * Sets the next handler in the chain.
     *
     * @param nextHandler The next handler to be executed.
     * @return The next handler, allowing for method chaining.
     */
    public DeveloperValidationHandler setNext(final DeveloperValidationHandler nextHandler) {
        this.next = nextHandler;
        return nextHandler;
    }

    /**
     * Checks the validation chain for the given assignment context.
     *
     * @param developer The developer being assigned.
     * @param ticket    The ticket being assigned.
     * @param milestone The milestone associated with the ticket.
     * @param command   The original command input.
     * @return true if all handlers pass, false if any handler finds an error.
     */
    public boolean check(final Developer developer, final Ticket ticket, final Milestone milestone,
            final CommandInput command) {
        final int error = validate(developer, ticket, milestone);
        if (error != 0) {
            showError(command, error);
            return false;
        }
        if (next != null) {
            return next.check(developer, ticket, milestone, command);
        }

        return true;
    }

    /**
     * Validates the specific rule for this handler.
     *
     * @param developer The developer being validated.
     * @param ticket    The ticket involved.
     * @param milestone The milestone involved.
     * @return 0 if valid, otherwise an error code.
     */
    protected abstract int validate(Developer developer, Ticket ticket, Milestone milestone);

    /**
     * Output the specific error message associated with the error code.
     *
     * @param command The command input.
     * @param error   The error code returned by validate.
     */
    protected abstract void showError(CommandInput command, int error);
}
