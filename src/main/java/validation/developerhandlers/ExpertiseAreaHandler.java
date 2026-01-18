package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

/**
 * Checks if the developer matches the expertise area of the ticket.
 */
public final class ExpertiseAreaHandler extends DeveloperValidationHandler {

    private static final int ERR_FRONTEND = 1;
    private static final int ERR_BACKEND = 2;
    private static final int ERR_DESIGN = 3;
    private static final int ERR_DB = 4;
    private static final int ERR_DEVOPS = 5;

    /**
     * Validates if the developer has the required expertise for the ticket.
     *
     * @param developer the developer to validate
     * @param ticket    the ticket to be assigned
     * @param milestone the milestone involved
     * @return 0 if valid, error code otherwise
     */
    @Override
    protected int validate(final Developer developer, final Ticket ticket,
            final Milestone milestone) {
        final String developerExpertise = developer.getExpertiseArea().name();
        final String ticketExpertise = ticket.getExpertiseArea().name();

        switch (ticketExpertise) {
            case "FRONTEND":
                if (!developerExpertise.equals("FRONTEND")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return ERR_FRONTEND;
                }
                return 0;
            case "BACKEND":
                if (!developerExpertise.equals("BACKEND")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return ERR_BACKEND;
                }
                return 0;
            case "DESIGN":
                if (!developerExpertise.equals("DESIGN")
                        && !developerExpertise.equals("FULLSTACK")
                        && !developerExpertise.equals("FRONTEND")) {
                    return ERR_DESIGN;
                }
                return 0;
            case "DB":
                if (!developerExpertise.equals("DB")
                        && !developerExpertise.equals("FULLSTACK")
                        && !developerExpertise.equals("BACKEND")) {
                    return ERR_DB;
                }
                return 0;
            case "DEVOPS":
                if (!developerExpertise.equals("DEVOPS")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return ERR_DEVOPS;
                }
                return 0;
            default:
                System.out.println("IMPLEMENT: in ExpertiseAreaHandler");
                return 0;

        }
    }

    /**
     * Shows the error message for expertise mismatch.
     *
     * @param command the command that caused the error
     * @param error   the error code
     */
    @Override
    protected void showError(final CommandInput command, final int error) {
        IOUtil.assignError(command, "EXPERTISE");
    }
}
