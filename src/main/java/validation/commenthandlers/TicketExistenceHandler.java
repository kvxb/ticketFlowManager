package validation.commenthandlers;

import io.CommandInput;

public class TicketExistenceHandler extends CommentValidationHandler {
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
