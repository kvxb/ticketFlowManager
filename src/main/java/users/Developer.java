package users;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import notifications.Observer;
import tickets.Ticket;
import java.time.temporal.TemporalAdjusters;

public class Developer extends User implements Observer {
    // TODO: format yyyy-mm-dd to be respected
    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB,
        FULLSTACK
    }

    public enum Seniority {
        JUNIOR,
        MID,
        SENIOR
    }

    private String hireDate;
    private List<String> notifications = new ArrayList<>();
    private boolean performanceScoreCalculated;

    private double performanceScore;

    private int closedTickets;

    private ExpertiseArea expertiseArea;

    private Seniority seniority;

    public Developer(String username, String email, String role, String hireDate, String expertiseArea,
            String seniority) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.expertiseArea = ExpertiseArea.valueOf(expertiseArea);
        this.seniority = Seniority.valueOf(seniority);
    }

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

    /** NOT YET IMPLEMENTED **/
    public List<Number> updatePerformanceScore(LocalDate reportTimestamp) {
        List<Number> stats = new ArrayList<>();

        int bugTickets = 0;
        int featureTickets = 0;
        int uiTickets = 0;
        int highPriorityTickets = 0;
        double totalResolutionTime = 0.0;
        int monthlyClosedTickets = 0;

        Database db = Database.getInstance();
        List<Ticket> allTickets = db.getAllTickets();

        LocalDate currentDate = reportTimestamp;
        LocalDate earliestDate = currentDate
                .withDayOfMonth(1)
                .minusMonths(1);
        LocalDate latestDate = currentDate
                .withDayOfMonth(1)
                .minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());

        for (Ticket ticket : allTickets) {
            if (ticket.getStatus() != Ticket.Status.CLOSED)
                continue;
            if (!ticket.getAssignedTo().equals(this.username))
                continue;

            LocalDate solvedDate = LocalDate.parse(ticket.getSolvedAt());

            if (solvedDate.isBefore(earliestDate) || solvedDate.isAfter(latestDate))
                continue;

            monthlyClosedTickets++;

            switch (ticket.getType()) {
                case "BUG":
                    bugTickets++;
                    break;
                case "FEATURE_REQUEST":
                    featureTickets++;
                    break;
                case "UI_FEEDBACK":
                    uiTickets++;
                    break;
            }

            if (ticket.getBusinessPriority() == Ticket.BusinessPriority.HIGH ||
                    ticket.getBusinessPriority() == Ticket.BusinessPriority.CRITICAL) {
                highPriorityTickets++;
            }

            LocalDate assignedDate = LocalDate.parse(ticket.getAssignedAt());
            long days = ChronoUnit.DAYS.between(assignedDate, solvedDate) + 1;
            totalResolutionTime += days;
        }

        double averageResolutionTime = monthlyClosedTickets > 0 ? totalResolutionTime / monthlyClosedTickets : 0.0;

        double seniorityBonus = switch (this.seniority) {
            case JUNIOR -> 5.0;
            case MID -> 15.0;
            case SENIOR -> 30.0;
            default -> 0.0;
        };

        double performanceScore = 0.0;

        switch (this.seniority) {
            case JUNIOR:
                if (monthlyClosedTickets > 0) {
                    double diversityFactor = ticketDiversityFactor(bugTickets, featureTickets, uiTickets);
                    performanceScore = Math.max(0, 0.5 * monthlyClosedTickets - diversityFactor) + seniorityBonus;
                } else {
                    performanceScore = 0.0;
                }
                break;

            case MID:
                if (monthlyClosedTickets > 0) {
                    performanceScore = Math.max(0, 0.5 * monthlyClosedTickets +
                            0.7 * highPriorityTickets -
                            0.3 * averageResolutionTime) + seniorityBonus;
                } else {
                    performanceScore = 0.0;
                }
                break;

            case SENIOR:
                if (monthlyClosedTickets > 0) {
                    performanceScore = Math.max(0, 0.5 * monthlyClosedTickets +
                            1.0 * highPriorityTickets -
                            0.5 * averageResolutionTime) + seniorityBonus;
                } else {
                    performanceScore = 0.0;
                }
                break;
        }

        this.performanceScore = performanceScore;
        this.performanceScoreCalculated = true;

        stats.add(monthlyClosedTickets);
        stats.add(performanceScore);
        stats.add(averageResolutionTime);
        return stats;
    }

    //TODO CHORE move where they belong
    public static double averageResolvedTicketType(int bug, int feature, int ui) {
        return (bug + feature + ui) / 3.0;
    }

    public static double standardDeviation(int bug, int feature, int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        double variance = (Math.pow(bug - mean, 2) + Math.pow(feature - mean, 2) + Math.pow(ui - mean, 2)) / 3.0;
        return Math.sqrt(variance);
    }

    public static double ticketDiversityFactor(int bug, int feature, int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);

        if (mean == 0.0) {
            return 0.0;
        }

        double std = standardDeviation(bug, feature, ui);
        return std / mean;
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

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }

    public boolean isPerformanceScoreCalculated() {
        return performanceScoreCalculated;
    }

    public void setPerformanceScoreCalculated(boolean performanceScoreCalculated) {
        this.performanceScoreCalculated = performanceScoreCalculated;
    }

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public int getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(int closedTickets) {
        this.closedTickets = closedTickets;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }
}
