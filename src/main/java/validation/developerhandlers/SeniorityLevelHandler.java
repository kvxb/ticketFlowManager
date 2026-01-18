package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

/**
 * Checks if the developer matches the seniority level of the ticket.
 */
public final class SeniorityLevelHandler extends DeveloperValidationHandler {

    private static final int DEV_LVL_MID = 3;
    private static final int DEV_LVL_SENIOR = 4;
    private static final int DEV_LVL_JUNIOR = 2;
    private static final int DEV_LVL_DEFAULT = -1;

    private static final int TICKET_LVL_LOW = 1;
    private static final int TICKET_LVL_MEDIUM = 2;
    private static final int TICKET_LVL_HIGH = 3;
    private static final int TICKET_LVL_CRITICAL = 4;
    private static final int TICKET_LVL_DEFAULT = 5;

    /**
     * Validates if the developer has the required seniority for the ticket.
     *
     * @param developer the developer to validate
     * @param ticket    the ticket to be assigned
     * @param milestone the milestone involved
     * @return 0 if valid, -1 otherwise
     */
    @Override
    protected int validate(final Developer developer, final Ticket ticket,
            final Milestone milestone) {
        final int developerLevel = switch (developer.getSeniority().name()) {
            case "MID" -> DEV_LVL_MID;
            case "SENIOR" -> DEV_LVL_SENIOR;
            case "JUNIOR" -> DEV_LVL_JUNIOR;
            default -> DEV_LVL_DEFAULT;
        };
        final int ticketLevel = switch (ticket.getBusinessPriority().name()) {
            case "LOW" -> TICKET_LVL_LOW;
            case "MEDIUM" -> TICKET_LVL_MEDIUM;
            case "HIGH" -> TICKET_LVL_HIGH;
            case "CRITICAL" -> TICKET_LVL_CRITICAL;
            default -> TICKET_LVL_DEFAULT;
        };
        if (developerLevel >= ticketLevel) {
            return 0;
        }
        return -1;
    }

    /**
     * Shows the error message for seniority mismatch.
     *
     * @param command the command that caused the error
     * @param error   the error code
     */
    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "SENIORITY");
    }
}
