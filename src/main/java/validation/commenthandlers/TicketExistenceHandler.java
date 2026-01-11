package validation.commenthandlers;

import io.CommandInput;
import database.Database;

public class TicketExistenceHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        return true;
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        return Database.getTicket(command.ticketID()) != null;
    }
    
    @Override
    protected void showError(CommandInput command) {
    }
}
