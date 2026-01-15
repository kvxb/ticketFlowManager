package validation.developerhandlers;

import tickets.Ticket;
import users.Developer;
import io.CommandInput;
import io.IOUtil;
import milestones.Milestone;

public class ExpertiseAreaHandler extends DeveloperValidationHandler {
    @Override
    protected int validate(Developer developer, Ticket ticket, Milestone milestone) {
        String developerExpertise = developer.getExpertiseArea().name();
        String ticketExpertise = ticket.getExpertiseArea().name();

        switch (ticketExpertise) {
            case "FRONTEND":
                if (!developerExpertise.equals("FRONTEND")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return 1;
                }
                return 0;
            case "BACKEND":
                if (!developerExpertise.equals("BACKEND")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return 2;
                }
                return 0;
            case "DESIGN":
                if (!developerExpertise.equals("DESIGN")
                        && !developerExpertise.equals("FULLSTACK")
                        && !developerExpertise.equals("FRONTEND")) {
                    return 3;
                }
                return 0;
            case "DB":
                if (!developerExpertise.equals("DB")
                        && !developerExpertise.equals("FULLSTACK")
                        && !developerExpertise.equals("BACKEND")) {
                    return 4;
                }
                return 0;
            case "DEVOPS":
                if (!developerExpertise.equals("DEVOPS")
                        && !developerExpertise.equals("FULLSTACK")) {
                    return 5;
                }
                return 0;
            default:
                System.out.println("IMPLEMENT: in ExpertiseAreaHandler");
                return 0;

        }
    }

    @Override
    protected void showError(CommandInput command, int error) {
        IOUtil.assignError(command, "EXPERTISE");
    }
}
