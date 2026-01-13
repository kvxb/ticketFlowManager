package users;

import tickets.Ticket;

public class Developer extends User {
    private String hireDate;

    // TODO: format yyyy-mm-dd to be respected
    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB,
        FULLSTACK
    }

    private ExpertiseArea expertiseArea;

    public enum Seniority {
        JUNIOR,
        MID,
        SENIOR
    }

    /**NOT YET IMPLEMENTED**/
    public double getPerformanceScore() {
        return 0.0;
    }

    public boolean canHandleTicket(Ticket ticket) {
        // TODO: the code is duplicated from a developer validation for seniority remove
        // it from there if can
        int developerLevel = switch (this.getSeniority().name()) {
            case "MID" -> 3;
            case "SENIOR" -> 4;
            case "JUNIOR" -> 2;
            default -> -1;
        };
        // if (developerLevel == -1) {
        // System.out.println("IMPLEMENT: SeniorityLevelHandler");
        // }
        int ticketLevel = switch (ticket.getBusinessPriority().name()) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            case "CRITICAL" -> 4;
            default -> 5;
        };
        if (developerLevel >= ticketLevel) {
            return true;
        }
        return false;
    }

    private Seniority seniority;

    public Developer(String username, String email, String role, String hireDate, String expertiseArea,
            String seniority) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.expertiseArea = ExpertiseArea.valueOf(expertiseArea);
        this.seniority = Seniority.valueOf(seniority);
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public void setSeniority(Seniority seniority) {
        this.seniority = seniority;
    }
}
