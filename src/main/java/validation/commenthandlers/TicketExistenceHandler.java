package validation.commenthandlers;

import io.CommandInput;

/**
 * Checks if the ticket is in the ticket system
 */
public final class TicketExistenceHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(final CommandInput command) {
        return true;
    }

    @Override
    protected boolean validateCondition(final CommandInput command) {
        return db.getTicket(command.ticketID()) != null;
    }

    @Override
    protected void showError(final CommandInput command) {
    }
}
