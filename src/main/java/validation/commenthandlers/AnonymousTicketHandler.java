package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import database.Database;
import tickets.Ticket;

public class AnonymousTicketHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        return true;
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        Ticket ticket = Database.getTicket(command.ticketID());
        return !ticket.getReportedBy().isEmpty();
    }
    
    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "ANON");
    }
}
