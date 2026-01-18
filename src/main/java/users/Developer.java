package users;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import lombok.Getter;
import lombok.Setter;
import notifications.Observer;
import tickets.Ticket;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Setter
@Getter
public final class Developer extends User implements Observer {

    private final Map<Integer, Integer> ticketCommentStats = new HashMap<>();
    private final Set<Integer> currentlyAssignedTickets = new HashSet<>();
    private static final double JUNIOR_BONUS = 5.0;
    private static final double MID_BONUS = 15.0;
    private static final double SENIOR_BONUS = 30.0;
    private static final double COEFF_CLOSED = 0.5;
    private static final double COEFF_PRIORITY_MID = 0.7;
    private static final double COEFF_TIME_MID = 0.3;
    private static final double COEFF_PRIORITY_SENIOR = 1.0;
    private static final double COEFF_TIME_SENIOR = 0.5;
    private static final double TYPES_COUNT = 3.0;

    private static final int LVL_JUNIOR = 2;
    private static final int LVL_MID = 3;
    private static final int LVL_SENIOR = 4;
    private static final int LVL_DEFAULT = -1;

    private static final int TICKET_LOW = 1;
    private static final int TICKET_MED = 2;
    private static final int TICKET_HIGH = 3;
    private static final int TICKET_CRIT = 4;
    private static final int TICKET_DEFAULT = 5;

    /**
     * Assigns the developer to a ticket.
     *
     * @param ticketId the ID of the ticket
     */
    public void assignToTicket(final int ticketId) {
        currentlyAssignedTickets.add(ticketId);
        ticketCommentStats.putIfAbsent(ticketId, 0);
    }

    /**
     * Removes the developer from a ticket assignment.
     *
     * @param ticketId the ID of the ticket
     */
    public void deassignFromTicket(final int ticketId) {
        currentlyAssignedTickets.remove(ticketId);
    }

    /**
     * Increments the comment count for a specific ticket if assigned.
     *
     * @param ticketId the ID of the ticket
     */
    public void incrementCommentCount(final int ticketId) {
        if (currentlyAssignedTickets.contains(ticketId)) {
            ticketCommentStats.put(ticketId,
                    ticketCommentStats.getOrDefault(ticketId, 0) + 1);
        }
    }

    /**
     * Gets the number of comments made on a specific ticket.
     *
     * @param ticketId the ID of the ticket
     * @return the number of comments
     */
    public int getCommentCountForTicket(final int ticketId) {
        return ticketCommentStats.getOrDefault(ticketId, 0);
    }

    /**
     * Gets the map of ticket comment statistics.
     *
     * @return a map of ticket IDs to comment counts
     */
    public Map<Integer, Integer> getTicketCommentStats() {
        return new HashMap<>(ticketCommentStats);
    }

    /**
     * Enum representing the area of expertise.
     */
    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB,
        FULLSTACK
    }

    /**
     * Enum representing the seniority level.
     */
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

    /**
     * Constructor for Developer.
     *
     * @param username      the username
     * @param email         the email
     * @param role          the role
     * @param hireDate      the hire date
     * @param expertiseArea the expertise area
     * @param seniority     the seniority level
     */
    public Developer(final String username, final String email, final String role,
            final String hireDate, final String expertiseArea,
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

    /**
     * Gets the list of notifications.
     *
     * @return list of notification strings
     */
    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Clears all notifications.
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * Updates the performance score based on a report timestamp.
     *
     * @param reportTimestamp the date of the report
     * @return a list of statistics numbers
     */
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
            if (ticket.getStatus() != Ticket.Status.CLOSED) {
                continue;
            }
            if (!ticket.getAssignedTo().equals(this.username)) {
                continue;
            }

            final LocalDate solvedDate = LocalDate.parse(ticket.getSolvedAt());

            if (solvedDate.isBefore(earliestDate) || solvedDate.isAfter(latestDate)) {
                continue;
            }

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
                default:
                    break;
            }

            if (ticket.getBusinessPriority() == Ticket.BusinessPriority.HIGH
                    || ticket.getBusinessPriority() == Ticket.BusinessPriority.CRITICAL) {
                highPriorityTickets++;
            }

            final LocalDate assignedDate = LocalDate.parse(ticket.getAssignedAt());
            final long days = ChronoUnit.DAYS.between(assignedDate, solvedDate) + 1;
            totalResolutionTime += days;
        }

        final double averageResolutionTime = monthlyClosedTickets > 0
                ? totalResolutionTime / monthlyClosedTickets
                : 0.0;

        final double seniorityBonus = switch (this.seniority) {
            case JUNIOR -> JUNIOR_BONUS;
            case MID -> MID_BONUS;
            case SENIOR -> SENIOR_BONUS;
            default -> 0.0;
        };

        double calculatedScore = 0.0;

        switch (this.seniority) {
            case JUNIOR:
                if (monthlyClosedTickets > 0) {
                    final double diversityFactor = ticketDiversityFactor(bugTickets,
                            featureTickets, uiTickets);
                    calculatedScore = Math.max(0, COEFF_CLOSED * monthlyClosedTickets
                            - diversityFactor) + seniorityBonus;
                } else {
                    calculatedScore = 0.0;
                }
                break;

            case MID:
                if (monthlyClosedTickets > 0) {
                    calculatedScore = Math.max(0, COEFF_CLOSED * monthlyClosedTickets
                            + COEFF_PRIORITY_MID * highPriorityTickets
                            - COEFF_TIME_MID * averageResolutionTime) + seniorityBonus;
                } else {
                    calculatedScore = 0.0;
                }
                break;

            case SENIOR:
                if (monthlyClosedTickets > 0) {
                    calculatedScore = Math.max(0, COEFF_CLOSED * monthlyClosedTickets
                            + COEFF_PRIORITY_SENIOR * highPriorityTickets
                            - COEFF_TIME_SENIOR * averageResolutionTime) + seniorityBonus;
                } else {
                    calculatedScore = 0.0;
                }
                break;
            default:
                break;
        }

        this.performanceScore = calculatedScore;
        this.performanceScoreCalculated = true;

        stats.add(monthlyClosedTickets);
        stats.add(calculatedScore);
        stats.add(averageResolutionTime);
        return stats;
    }

    /**
     * Calculates the average of three ticket types counts.
     *
     * @param bug     count of bugs
     * @param feature count of features
     * @param ui      count of ui feedbacks
     * @return the average
     */
    public static double averageResolvedTicketType(final int bug, final int feature, final int ui) {
        return (bug + feature + ui) / TYPES_COUNT;
    }

    /**
     * Calculates the standard deviation of ticket types counts.
     *
     * @param bug     count of bugs
     * @param feature count of features
     * @param ui      count of ui feedbacks
     * @return the standard deviation
     */
    public static double standardDeviation(final int bug, final int feature, final int ui) {
        final double mean = averageResolvedTicketType(bug, feature, ui);
        final double variance = (Math.pow(bug - mean, 2)
                + Math.pow(feature - mean, 2)
                + Math.pow(ui - mean, 2)) / TYPES_COUNT;
        return Math.sqrt(variance);
    }

    /**
     * Calculates the ticket diversity factor.
     *
     * @param bug     count of bugs
     * @param feature count of features
     * @param ui      count of ui feedbacks
     * @return the diversity factor
     */
    public static double ticketDiversityFactor(final int bug, final int feature, final int ui) {
        final double mean = averageResolvedTicketType(bug, feature, ui);

        if (mean == 0.0) {
            return 0.0;
        }

        final double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    /**
     * Checks if the developer can handle a specific ticket based on
     * seniority/complexity.
     *
     * @param ticket the ticket to check
     * @return true if the developer can handle it, false otherwise
     */
    public boolean canHandleTicket(final Ticket ticket) {
        final int developerLevel = switch (this.getSeniority().name()) {
            case "MID" -> LVL_MID;
            case "SENIOR" -> LVL_SENIOR;
            case "JUNIOR" -> LVL_JUNIOR;
            default -> LVL_DEFAULT;
        };

        final int ticketLevel = switch (ticket.getBusinessPriority().name()) {
            case "LOW" -> TICKET_LOW;
            case "MEDIUM" -> TICKET_MED;
            case "HIGH" -> TICKET_HIGH;
            case "CRITICAL" -> TICKET_CRIT;
            default -> TICKET_DEFAULT;
        };
        if (developerLevel >= ticketLevel) {
            return true;
        }
        return false;
    }

}
