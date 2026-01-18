package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

/**
 * Checks if the milestone of the ticket is unlocked
 */
public final class LockedMilestoneHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(final Developer developer, final Ticket ticket,
            final Milestone milestone) {
        return milestone.isBlocked() ? -1 : 0;
    }

    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "LOCKED");
    }
}
