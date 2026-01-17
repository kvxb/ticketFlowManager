package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

public class TicketStatusHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(final Developer developer, final Ticket ticket, final Milestone milestone) {
        return ticket.getStatus().name().equals("OPEN") ? 0 : 1;
    }

    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "STATUS");
    }
}
