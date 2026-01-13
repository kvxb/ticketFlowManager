package users;

import notifications.Observer;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;

public class Developer extends User implements Observer {
    private String hireDate;
    private List<String> notifications = new ArrayList<>();

    @Override
    public void update(String message) {
        notifications.add(message);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

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

    /** NOT YET IMPLEMENTED **/
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
