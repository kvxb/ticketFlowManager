package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import milestones.Milestone;

public abstract class DeveloperValidationHandler {
    private DeveloperValidationHandler next;
    
    public DeveloperValidationHandler setNext(final DeveloperValidationHandler next) {
        this.next = next;
        return next;
    }
    
    public boolean check(final Developer developer, final Ticket ticket, final Milestone milestone, final CommandInput command) {
        final int error = validate(developer, ticket, milestone);
        if(error != 0){   
            showError(command, error);
            return false;
        }
        
        if (next != null) {
            return next.check(developer, ticket, milestone, command);
        }
        
        return true;
    }
    
    protected abstract int validate(Developer developer, Ticket ticket, Milestone milestone);
    protected abstract void showError(CommandInput command, int error);
}
