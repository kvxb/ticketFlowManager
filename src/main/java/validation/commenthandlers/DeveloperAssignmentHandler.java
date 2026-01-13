package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import tickets.Ticket;
import users.User;

public class DeveloperAssignmentHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        User user = db.getUser(command.username());
        return user.getRole().name().equals("DEVELOPER");
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        Ticket ticket = db.getTicket(command.ticketID());
        
        String assignedTo = ticket.getAssignedTo();
        
        return assignedTo != null && assignedTo.equals(command.username());
    }
    
    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "ASSIGNMENT_DEVELOPER");
    }
}
