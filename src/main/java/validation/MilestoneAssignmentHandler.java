package validation;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import milestones.Milestone;
import io.IOUtil;

public class MilestoneAssignmentHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(Developer developer, Ticket ticket, Milestone milestone) {
        return milestone.hasDeveloper(developer.getUsername()) ? 0 : -1;
    }

    @Override
    protected void showError(CommandInput command, int error) {
        IOUtil.assignError(command, "ASSIGNMENT_");
    }
}
