package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import tickets.Ticket;
import users.User;

/**
 * Checks if the reporter is the one who reported the ticket
 */
public final class ReporterOwnershipHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(final CommandInput command) {
        final User user = db.getUser(command.username());
        return user.getRole().name().equals("REPORTER");
    }

    @Override
    protected boolean validateCondition(final CommandInput command) {
        final Ticket ticket = db.getTicket(command.ticketID());

        final String reportedBy = ticket.getReportedBy();

        return reportedBy != null && reportedBy.equals(command.username());
    }

    @Override
    protected void showError(final CommandInput command) {
        IOUtil.commentError(command, "ASSIGNMENT_REPORTER");
    }
}
