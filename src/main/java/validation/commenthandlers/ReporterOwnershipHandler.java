package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;
import database.Database;
import tickets.Ticket;
import users.User;
import users.Reporter;

public class ReporterOwnershipHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        User user = Database.getUser(command.username());
        return user.getRole().name().equals("REPORTER");
    }

    @Override
    protected boolean validateCondition(CommandInput command) {
        Ticket ticket = Database.getTicket(command.ticketID());

        String reportedBy = ticket.getReportedBy();

        return reportedBy != null && reportedBy.equals(command.username());
    }

    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "ASSIGNMENT_REPORTER");
    }
}
