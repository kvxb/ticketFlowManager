package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import database.Database;
import tickets.Ticket;
import users.User;
import users.Developer;

public class DeveloperAssignmentHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        User user = Database.getUser(command.username());
        return user.getRole().name().equals("DEVELOPER");
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        Ticket ticket = Database.getTicket(command.ticketID());
        
        String assignedTo = ticket.getAssignedTo();
        
        return assignedTo != null && assignedTo.equals(command.username());
    }
    
    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "ASSIGNMENT_DEVELOPER");
    }
}
