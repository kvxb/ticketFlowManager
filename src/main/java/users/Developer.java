package users;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import notifications.Observer;
import tickets.Ticket;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class Developer extends User implements Observer {

    private final Map<Integer, Integer> ticketCommentStats = new HashMap<>();
    private final Set<Integer> currentlyAssignedTickets = new HashSet<>();

    public void assignToTicket(final int ticketId) {
        currentlyAssignedTickets.add(ticketId);
        ticketCommentStats.putIfAbsent(ticketId, 0);
    }

    public void deassignFromTicket(final int ticketId) {
        currentlyAssignedTickets.remove(ticketId);
    }

    public void incrementCommentCount(final int ticketId) {
        if (currentlyAssignedTickets.contains(ticketId)) {
            ticketCommentStats.put(ticketId, ticketCommentStats.getOrDefault(ticketId, 0) + 1);
        }
    }

    public int getCommentCountForTicket(final int ticketId) {
        return ticketCommentStats.getOrDefault(ticketId, 0);
    }

    public Map<Integer, Integer> getTicketCommentStats() {
        return new HashMap<>(ticketCommentStats);
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

    public Developer(final String username, final String email, final String role, final String hireDate,
            final String expertiseArea,
            final String seniority) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.expertiseArea = ExpertiseArea.valueOf(expertiseArea);
        this.seniority = Seniority.valueOf(seniority);
    }

    @Override
    public void update(final String message) {
        notifications.add(message);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }

    /** NOT YET IMPLEMENTED **/
    public List<Number> updatePerformanceScore(final LocalDate reportTimestamp) {
        final List<Number> stats = new ArrayList<>();

        int bugTickets = 0;
        int featureTickets = 0;
        int uiTickets = 0;
        int highPriorityTickets = 0;
        double totalResolutionTime = 0.0;
        int monthlyClosedTickets = 0;

        final Database db = Database.getInstance();
        final List<Ticket> allTickets = db.getAllTickets();

        final LocalDate currentDate = reportTimestamp;
        final LocalDate earliestDate = currentDate
                .withDayOfMonth(1)
                .minusMonths(1);
        final LocalDate latestDate = currentDate
                .withDayOfMonth(1)
                .minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());

        for (final Ticket ticket : allTickets) {
            if (ticket.getStatus() != Ticket.Status.CLOSED)
                continue;
            if (!ticket.getAssignedTo().equals(this.username))
                continue;

            final LocalDate solvedDate = LocalDate.parse(ticket.getSolvedAt());

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

            final LocalDate assignedDate = LocalDate.parse(ticket.getAssignedAt());
            final long days = ChronoUnit.DAYS.between(assignedDate, solvedDate) + 1;
            totalResolutionTime += days;
        }

        final double averageResolutionTime = monthlyClosedTickets > 0 ? totalResolutionTime / monthlyClosedTickets
                : 0.0;

        final double seniorityBonus = switch (this.seniority) {
            case JUNIOR -> 5.0;
            case MID -> 15.0;
            case SENIOR -> 30.0;
            default -> 0.0;
        };

        double performanceScore = 0.0;

        switch (this.seniority) {
            case JUNIOR:
                if (monthlyClosedTickets > 0) {
                    final double diversityFactor = ticketDiversityFactor(bugTickets, featureTickets, uiTickets);
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

    // TODO CHORE move where they belong
    public static double averageResolvedTicketType(final int bug, final int feature, final int ui) {
        return (bug + feature + ui) / 3.0;
    }

    public static double standardDeviation(final int bug, final int feature, final int ui) {
        final double mean = averageResolvedTicketType(bug, feature, ui);
        final double variance = (Math.pow(bug - mean, 2) + Math.pow(feature - mean, 2) + Math.pow(ui - mean, 2)) / 3.0;
        return Math.sqrt(variance);
    }

    public static double ticketDiversityFactor(final int bug, final int feature, final int ui) {
        final double mean = averageResolvedTicketType(bug, feature, ui);

        if (mean == 0.0) {
            return 0.0;
        }

        final double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    public boolean canHandleTicket(final Ticket ticket) {
        // TODO: the code is duplicated from a developer validation for seniority remove
        // it from there if can
        final int developerLevel = switch (this.getSeniority().name()) {
            case "MID" -> 3;
            case "SENIOR" -> 4;
            case "JUNIOR" -> 2;
            default -> -1;
        };
        // if (developerLevel == -1) {
        // System.out.println("IMPLEMENT: SeniorityLevelHandler");
        // }
        final int ticketLevel = switch (ticket.getBusinessPriority().name()) {
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

    public void setHireDate(final String hireDate) {
        this.hireDate = hireDate;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(final ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public void setSeniority(final Seniority seniority) {
        this.seniority = seniority;
    }

    public void setNotifications(final List<String> notifications) {
        this.notifications = notifications;
    }

    public boolean isPerformanceScoreCalculated() {
        return performanceScoreCalculated;
    }

    public void setPerformanceScoreCalculated(final boolean performanceScoreCalculated) {
        this.performanceScoreCalculated = performanceScoreCalculated;
    }

    public void setPerformanceScore(final double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public int getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(final int closedTickets) {
        this.closedTickets = closedTickets;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }
}
