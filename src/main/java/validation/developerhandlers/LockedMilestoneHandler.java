package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

//TODO: QUESTION maybe rename this to blocked ? 
public class LockedMilestoneHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(Developer developer, Ticket ticket, Milestone milestone) {
        return milestone.isBlocked()?-1:0;
    }
    
    @Override
    protected void showError(CommandInput command, int error) {
        IOUtil.assignError(command, "LOCKED" );
    }
}
