package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import tickets.Ticket;
import users.User;

public class ClosedTicketHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(final CommandInput command) {
        final User user = db.getUser(command.username());
        return user.getRole().name().equals("REPORTER");
    }

    @Override
    protected boolean validateCondition(final CommandInput command) {
        final Ticket ticket = db.getTicket(command.ticketID());
        System.out.println(ticket.getStatus().name());
        return !ticket.getStatus().name().equals("CLOSED");
    }

    @Override
    protected void showError(final CommandInput command) {
        IOUtil.commentError(command, "CLOSED");
    }
}
