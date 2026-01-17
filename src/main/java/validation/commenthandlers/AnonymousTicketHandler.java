package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import database.Database;
import tickets.Ticket;

public class AnonymousTicketHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(final CommandInput command) {
        return true;
    }

    @Override
    protected boolean validateCondition(final CommandInput command) {
        final Ticket ticket = db.getTicket(command.ticketID());
        return !ticket.getReportedBy().isEmpty();
    }

    @Override
    protected void showError(final CommandInput command) {
        IOUtil.commentError(command, "ANON");
    }
}
