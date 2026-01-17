package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import milestones.Milestone;
import io.IOUtil;

public class MilestoneAssignmentHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(final Developer developer, final Ticket ticket, final Milestone milestone) {
        return milestone.hasDeveloper(developer.getUsername()) ? 0 : -1;
    }

    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "ASSIGNMENT");
    }
}
