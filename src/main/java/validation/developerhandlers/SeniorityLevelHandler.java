package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

// check what this needs
public class SeniorityLevelHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(final Developer developer, final Ticket ticket, final Milestone milestone) {
        final int developerLevel = switch (developer.getSeniority().name()) {
            case "MID" -> 3;
            case "SENIOR" -> 4;
            case "JUNIOR" -> 2;
            default -> -1;
        };
        final int ticketLevel = switch (ticket.getBusinessPriority().name()) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            case "CRITICAL" -> 4;
            default -> 5;
        };
        if (developerLevel >= ticketLevel) {
            return 0;
        }
        return -1;
    }

    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "SENIORITY");
    }
}
