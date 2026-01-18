package tickets;

import java.util.ArrayList;
import java.util.List;

import io.CommandInput;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class representing a generic Ticket.
 */
@Getter
@Setter
public abstract class Ticket {

    /**
     * Calculates the impact score of the ticket.
     *
     * @return the impact score
     */
    public abstract double getImpact();

    /**
     * Calculates the risk score of the ticket.
     *
     * @return the risk score
     */
    public abstract double getRisk();

    /**
     * Calculates the efficiency score of the ticket.
     *
     * @return the efficiency score
     */
    public abstract double getEfficiency();

    /**
     * Enum for Business Priority.
     */
    public enum BusinessPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Enum for Ticket Status.
     */
    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    /**
     * Enum for Expertise Area.
     */
    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB
    }

    /**
     * Abstract Builder class for Ticket.
     *
     * @param <T> the type of the builder
     */
    public abstract static class Builder<T extends Builder<T>> {
        protected int id;
        protected String type;
        protected String title;
        protected BusinessPriority businessPriority;
        protected Status status = Status.OPEN;
        protected ExpertiseArea expertiseArea;
        protected String description;
        protected String reportedBy;
        protected String createdAt;

        /**
         * Sets the ID.
         *
         * @param val the id
         * @return the builder
         */
        public T id(final int val) {
            this.id = val;
            return self();
        }

        /**
         * Sets the creation date.
         *
         * @param val the date string
         * @return the builder
         */
        public T createdAt(final String val) {
            this.createdAt = val;
            return self();
        }

        /**
         * Sets the type.
         *
         * @param val the type string
         * @return the builder
         */
        public T type(final String val) {
            this.type = val;
            return self();
        }

        /**
         * Sets the title.
         *
         * @param val the title
         * @return the builder
         */
        public T title(final String val) {
            this.title = val;
            return self();
        }

        /**
         * Sets the business priority.
         *
         * @param val the priority
         * @return the builder
         */
        public T businessPriority(final BusinessPriority val) {
            this.businessPriority = val;
            return self();
        }

        /**
         * Sets the status.
         *
         * @param val the status
         * @return the builder
         */
        public T status(final Status val) {
            this.status = val;
            return self();
        }

        /**
         * Sets the expertise area.
         *
         * @param val the area
         * @return the builder
         */
        public T expertiseArea(final ExpertiseArea val) {
            this.expertiseArea = val;
            return self();
        }

        /**
         * Sets the description.
         *
         * @param val the description
         * @return the builder
         */
        public T description(final String val) {
            this.description = val;
            return self();
        }

        /**
         * Sets the reporter.
         *
         * @param val the reporter username
         * @return the builder
         */
        public T reportedBy(final String val) {
            this.reportedBy = val;
            return self();
        }

        /**
         * Builds the ticket.
         *
         * @return the ticket
         */
        public abstract Ticket build();

        /**
         * Gets the ID.
         *
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * Sets the ID.
         *
         * @param id the id
         */
        public void setId(final int id) {
            this.id = id;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the type.
         *
         * @param type the type
         */
        public void setType(final String type) {
            this.type = type;
        }

        /**
         * Gets the title.
         *
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the title.
         *
         * @param title the title
         */
        public void setTitle(final String title) {
            this.title = title;
        }

        /**
         * Gets the business priority.
         *
         * @return the priority
         */
        public BusinessPriority getBusinessPriority() {
            return businessPriority;
        }

        /**
         * Sets the business priority.
         *
         * @param businessPriority the priority
         */
        public void setBusinessPriority(final BusinessPriority businessPriority) {
            this.businessPriority = businessPriority;
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Sets the status.
         *
         * @param status the status
         */
        public void setStatus(final Status status) {
            this.status = status;
        }

        /**
         * Gets the expertise area.
         *
         * @return the area
         */
        public ExpertiseArea getExpertiseArea() {
            return expertiseArea;
        }

        /**
         * Sets the expertise area.
         *
         * @param expertiseArea the area
         */
        public void setExpertiseArea(final ExpertiseArea expertiseArea) {
            this.expertiseArea = expertiseArea;
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the description.
         *
         * @param description the description
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * Gets the reporter.
         *
         * @return the reporter
         */
        public String getReportedBy() {
            return reportedBy;
        }

        /**
         * Sets the reporter.
         *
         * @param reportedBy the reporter
         */
        public void setReportedBy(final String reportedBy) {
            this.reportedBy = reportedBy;
        }

        /**
         * Returns the builder instance.
         *
         * @return this
         */
        protected abstract T self();
    }

    /**
     * Represents an action taken on a ticket.
     */
    @Setter
    @Getter
    public class Action {
        private String milestone;
        private String by;
        private String timestamp;
        private String action;
        private Status from;
        private Status to;

        /**
         * Default constructor.
         */
        public Action() {

        }

        /**
         * Constructor for milestone actions.
         *
         * @param milestone the milestone name
         * @param by        the user
         * @param timestamp the time
         * @param action    the action type
         */
        public Action(final String milestone, final String by, final String timestamp,
                final String action) {
            this.milestone = milestone;
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

        /**
         * Constructor for status change actions.
         *
         * @param from      old status
         * @param to        new status
         * @param by        the user
         * @param timestamp the time
         * @param action    the action type
         */
        public Action(final Status from, final Status to, final String by, final String timestamp,
                final String action) {
            this.from = from;
            this.to = to;
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

        /**
         * Constructor for simple actions.
         *
         * @param by        the user
         * @param timestamp the time
         * @param action    the action type
         */
        public Action(final String by, final String timestamp, final String action) {
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

    }

    /**
     * Represents the history of a ticket.
     */
    @Getter
    @Setter
    public class TicketHistory {

        private int historyId;
        private String historyTitle;
        private Status historyStatus;
        private List<Action> actions = new ArrayList<Action>();

        /**
         * Sets the history ID.
         *
         * @param id the id
         */
        public void setId(final int id) {
            this.historyId = id;
        }

        /**
         * Sets the history title.
         *
         * @param title the title
         */
        public void setTitle(final String title) {
            this.historyTitle = title;
        }

        /**
         * Sets the history status.
         *
         * @param status the status
         */
        public void setStatus(final Status status) {
            this.historyStatus = status;
        }
    }

    /**
     * Represents a comment on a ticket.
     */
    @Setter
    @Getter
    public class Comment {
        private String author;
        private String content;
        private String createdAt;

        /**
         * Constructor for Comment.
         *
         * @param author    the author
         * @param content   the content
         * @param createdAt the creation time
         */
        public Comment(final String author, final String content, final String createdAt) {
            this.author = author;
            this.createdAt = createdAt;
            this.content = content;
        }

    }

    private static int ticketIdCounter = 0;

    /**
     * Gets the next ticket ID and increments the counter.
     *
     * @return the next ID
     */
    public static int getNextTicketId() {
        final int answer = ticketIdCounter;
        ticketIdCounter++;
        return answer;
    }

    /**
     * Resets the ticket ID counter.
     */
    public static void clearTicket() {
        ticketIdCounter = 0;
    }

    /**
     * Gets the current ticket ID counter.
     *
     * @return the counter
     */
    public static int getTicketId() {
        return ticketIdCounter;
    }

    /**
     * Sets the ticket ID counter.
     *
     * @param val the value
     */
    public static void setTicketId(final int val) {
        Ticket.ticketIdCounter = val;
    }

    private int id;
    private String type;
    private String title;
    private String description;
    private String reportedBy;
    protected BusinessPriority businessPriority;
    private Status status;
    private String assignedTo;
    private String assignedAt;
    private String solvedAt;
    private String createdAt;
    private ExpertiseArea expertiseArea;
    private List<Comment> comments = new ArrayList<Comment>();
    private TicketHistory ticketHistory;

    /**
     * Constructor using Builder.
     *
     * @param b the builder
     */
    public Ticket(final Builder<?> b) {
        this.id = b.id;
        this.type = b.type;
        this.title = b.title;
        this.businessPriority = b.businessPriority;
        this.status = b.status;
        this.expertiseArea = b.expertiseArea;
        this.description = b.description;
        this.reportedBy = b.reportedBy;
        this.createdAt = b.createdAt;

        this.ticketHistory = new TicketHistory();
        this.ticketHistory.setId(this.id);
        this.ticketHistory.setTitle(this.title);
        this.ticketHistory.setStatus(this.status);

    }

    /**
     * Adds a milestone action to the history.
     *
     * @param milestone the milestone name
     * @param by        the user
     * @param timestamp the time
     */
    public void addActionMilestone(final String milestone, final String by,
            final String timestamp) {
        ticketHistory.actions.add(new Action(milestone, by, timestamp, "ADDED_TO_MILESTONE"));
    }

    /**
     * Gets the list of comments.
     *
     * @return the comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Sets the list of comments.
     *
     * @param comments the comments
     */
    public void setComments(final List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * Assigns a developer to the ticket.
     *
     * @param command the command containing assignment details
     */
    public void assignDeveloper(final CommandInput command) {
        final Action assignAction = new Action(command.username(), command.timestamp(), "ASSIGNED");
        ticketHistory.getActions().add(assignAction);

        final Action statusAction = new Action(Status.OPEN, Status.IN_PROGRESS,
                command.username(), command.timestamp(), "STATUS_CHANGED");
        ticketHistory.getActions().add(statusAction);

        this.status = Status.IN_PROGRESS;
        this.assignedTo = command.username();
        this.assignedAt = command.timestamp();
    }

    /**
     * Undoes the assignment of a developer.
     *
     * @param command the command containing undo details
     */
    public void undoAssignDeveloper(final CommandInput command) {
        final Action assignAction = new Action(command.username(), command.timestamp(),
                "DE-ASSIGNED");
        ticketHistory.getActions().add(assignAction);

        final Action statusAction = new Action(Status.IN_PROGRESS, Status.OPEN,
                command.username(), command.timestamp(), "STATUS_CHANGED");
        ticketHistory.getActions().add(statusAction);

        this.status = Status.OPEN;
        this.assignedTo = null;
        this.assignedAt = null;
    }

    /**
     * Changes the status of the ticket.
     *
     * @param newStatus the new status
     * @param by        the user changing the status
     * @param timestamp the time of change
     */
    public void changeStatus(final Status newStatus, final String by, final String timestamp) {
        if (this.ticketHistory != null) {
            final Action statusAction = new Action(this.status, newStatus, by, timestamp,
                    "STATUS_CHANGED");
            this.ticketHistory.getActions().add(statusAction);
        }
        this.status = newStatus;
        if (status.name().equals("RESOLVED")) {
            solvedAt = timestamp;
        }
    }

    /**
     * Increases the priority of the ticket.
     */
    public void upPriority() {
        businessPriority = switch (businessPriority) {
            case BusinessPriority.LOW -> BusinessPriority.MEDIUM;
            case BusinessPriority.MEDIUM -> BusinessPriority.HIGH;
            case BusinessPriority.HIGH -> BusinessPriority.CRITICAL;
            default -> BusinessPriority.CRITICAL;
        };
    }

    /**
     * Gets the required expertise string representation.
     *
     * @return the required expertise
     */
    public String getRequiredExpertise() {
        if (this.getExpertiseArea() == null) {
            return "";
        }
        switch (this.getExpertiseArea()) {
            case FRONTEND:
                return "FRONTEND, FULLSTACK, DESIGN";
            case BACKEND:
                return "BACKEND, FULLSTACK";
            case DESIGN:
                return "DESIGN, FULLSTACK, FRONTEND";
            case DB:
                return "BACKEND, DB, FULLSTACK";
            case DEVOPS:
                return "DEVOPS, FULLSTACK";
            default:
                return "";
        }
    }

    /**
     * Gets the required seniority string representation.
     *
     * @return the required seniority
     */
    public String getRequiredSeniority() {
        if (this.getBusinessPriority() == null) {
            return "";
        }
        switch (this.getBusinessPriority()) {
            case LOW:
                return "JUNIOR, MID, SENIOR";
            case MEDIUM:
                return "JUNIOR, MID, SENIOR";
            case HIGH:
                return "MID, SENIOR";
            case CRITICAL:
                return "SENIOR";
            default:
                return "";
        }
    }

    /**
     * Adds a comment to the ticket.
     *
     * @param author  the author
     * @param comment the content
     * @param date    the date
     */
    public void addComment(final String author, final String comment, final String date) {
        comments.add(new Comment(author, comment, date));
    }

    /**
     * Undoes the last comment by the author.
     *
     * @param author the author
     */
    public void undoAddComment(final String author) {
        Comment remove = null;
        for (final Comment c : comments) {
            if (c.author.equals(author)) {
                remove = c;
            }
        }
        if (remove != null) {
            System.out.println("undid comment");
            comments.remove(remove);
        }
    }

    private List<String> matchingWords = new ArrayList<>();

}
