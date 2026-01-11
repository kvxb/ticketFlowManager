package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import database.Database;
import tickets.Ticket;
import users.User;

public class ClosedTicketHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        User user = Database.getUser(command.username());
        return user.getRole().name().equals("Reporter") ;
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        Ticket ticket = Database.getTicket(command.ticketID());
        return ticket.getStatus().name().equals("CLOSED");
    }
    
    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "CLOSED");
    }
}
