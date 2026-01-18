package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

/**
 * Checks if the ticket is open for (re)assignment
 */
public final class TicketStatusHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(final Developer developer, final Ticket ticket,
            final Milestone milestone) {
        return ticket.getStatus().name().equals("OPEN") ? 0 : 1;
    }

    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "STATUS");
    }
}
