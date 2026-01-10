package validation;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

public class TicketStatusHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(Developer developer, Ticket ticket, Milestone milestone) {
        return ticket.getStatus().name().equals("OPEN")?0:1;
    }
    
    @Override
    protected void showError(CommandInput command, int error) {
        IOUtil.assignError(command, "STATUS");
    }
}
