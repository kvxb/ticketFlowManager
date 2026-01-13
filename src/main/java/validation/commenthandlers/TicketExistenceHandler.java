package validation.commenthandlers;

import io.CommandInput;

public class TicketExistenceHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        return true;
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        return db.getTicket(command.ticketID()) != null;
    }
    
    @Override
    protected void showError(CommandInput command) {
    }
}
