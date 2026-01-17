package tickets;

import java.util.ArrayList;
import java.util.List;

import io.CommandInput;

public abstract class Ticket {

    public abstract double getImpact();

    public abstract double getRisk();

    public abstract double getEfficiency();

    public enum BusinessPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    public enum ExpertiseArea {
        FRONTEND,
        BACKEND,
        DEVOPS,
        DESIGN,
        DB
    }

    // maybe get this the fuck out of here
    public abstract static class Builder<T extends Builder<T>> {
        protected int id;
        protected String type;
        protected String title;
        protected BusinessPriority businessPriority;
        protected Status status = Status.OPEN;
        protected ExpertiseArea expertiseArea;
        protected String description;
        protected String reportedBy;
        // TODO: shouldnt this be a LocalDate?
        protected String createdAt;

        public T id(final int id) {
            this.id = id;
            return self();
        }

        public T createdAt(final String createdAt) {
            this.createdAt = createdAt;
            return self();
        }

        public T type(final String type) {
            this.type = type;
            return self();
        }

        public T title(final String title) {
            this.title = title;
            return self();
        }

        public T businessPriority(final BusinessPriority priority) {
            this.businessPriority = priority;
            return self();
        }

        public T status(final Status status) {
            this.status = status;
            return self();
        }

        public T expertiseArea(final ExpertiseArea area) {
            this.expertiseArea = area;
            return self();
        }

        public T description(final String description) {
            this.description = description;
            return self();
        }

        public T reportedBy(final String reportedBy) {
            this.reportedBy = reportedBy;
            return self();
        }

        public abstract Ticket build();

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public BusinessPriority getBusinessPriority() {
            return businessPriority;
        }

        public void setBusinessPriority(final BusinessPriority businessPriority) {
            this.businessPriority = businessPriority;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }

        public ExpertiseArea getExpertiseArea() {
            return expertiseArea;
        }

        public void setExpertiseArea(final ExpertiseArea expertiseArea) {
            this.expertiseArea = expertiseArea;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getReportedBy() {
            return reportedBy;
        }

        public void setReportedBy(final String reportedBy) {
            this.reportedBy = reportedBy;
        }

        protected abstract T self();
    }

    public class Action {
        String milestone;
        String by;
        String timestamp;
        String action;
        Status from;
        Status to;

        public Action() {

        }

        public Action(final String milestone, final String by, final String timestamp, final String action) {
            this.milestone = milestone;
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

        public Action(final Status from, final Status to, final String by, final String timestamp,
                final String action) {
            this.from = from;
            this.to = to;
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

        public Action(final String by, final String timestamp, final String action) {
            this.by = by;
            this.timestamp = timestamp;
            this.action = action;
        }

        public String getMilestone() {
            return milestone;
        }

        public void setMilestone(final String milestone) {
            this.milestone = milestone;
        }

        public String getBy() {
            return by;
        }

        public void setBy(final String by) {
            this.by = by;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(final String timestamp) {
            this.timestamp = timestamp;
        }

        public String getAction() {
            return action;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public Status getFrom() {
            return from;
        }

        public void setFrom(final Status from) {
            this.from = from;
        }

        public Status getTo() {
            return to;
        }

        public void setTo(final Status to) {
            this.to = to;
        }
    }

    public class TicketHistory {

        int id;
        String title;
        Status status;
        private List<Action> actions = new ArrayList<Action>();

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }

        public List<Action> getActions() {
            return actions;
        }

        public void setActions(final List<Action> actions) {
            this.actions = actions;
        }

    }

    public class Comment {
        String author;
        String content;
        String createdAt;

        public Comment(final String author, final String content, final String createdAt) {
            this.author = author;
            this.createdAt = createdAt;
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(final String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(final String content) {
            this.content = content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(final String createdAt) {
            this.createdAt = createdAt;
        }
    }

    private static int ticketId = 0;

    public static int getNextTicketId() {
        final int answer = ticketId;
        ticketId++;
        return answer;
    }

    public static void clearTicket() {
        ticketId = 0;
    }

    public static int getTicketId() {
        return ticketId;
    }

    public static void setTicketId(final int ticketId) {
        Ticket.ticketId = ticketId;
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

    public void addActionMilestone(final String milestone, final String by, final String timestamp) {
        ticketHistory.actions.add(new Action(milestone, by, timestamp, "ADDED_TO_MILESTONE"));
    }

    public List<Comment> getComments() {
        return comments;
    }

    // TODO imlement the comments part

    public void setComments(final List<Comment> comments) {
        this.comments = comments;
    }

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

    public void undoAssignDeveloper(final CommandInput command) {
        final Action assignAction = new Action(command.username(), command.timestamp(), "DE-ASSIGNED");
        ticketHistory.getActions().add(assignAction);

        final Action statusAction = new Action(Status.IN_PROGRESS, Status.OPEN,
                command.username(), command.timestamp(), "STATUS_CHANGED");
        ticketHistory.getActions().add(statusAction);

        this.status = Status.OPEN;
        this.assignedTo = null;
        this.assignedAt = null;
    }

    public void changeStatus(final Status newStatus, final String by, final String timestamp) {
        if (this.ticketHistory != null) {
            final Action statusAction = new Action(this.status, newStatus, by, timestamp, "STATUS_CHANGED");
            this.ticketHistory.getActions().add(statusAction);
        }
        this.status = newStatus;
        if (status.name().equals("RESOLVED")) {
            solvedAt = timestamp;
        }
    }

    public void upPriority() {
        businessPriority = switch (businessPriority) {
            case BusinessPriority.LOW -> BusinessPriority.MEDIUM;
            case BusinessPriority.MEDIUM -> BusinessPriority.HIGH;
            case BusinessPriority.HIGH -> BusinessPriority.CRITICAL;
            default -> BusinessPriority.CRITICAL;
        };
    }

    public String getRequiredExpertise() {
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

    public String getRequiredSeniority() {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void addComment(final String author, final String comment, final String date) {
        comments.add(new Comment(author, comment, date));
    }

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

    public List<String> getMatchingWords() {
        return matchingWords;
    }

    public void setMatchingWords(final List<String> matchingWords) {
        this.matchingWords = matchingWords;
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(final String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(final String solvedAt) {
        this.solvedAt = solvedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(final String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    public void setBusinessPriority(final BusinessPriority businessPriority) {
        this.businessPriority = businessPriority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(final ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public TicketHistory getTicketHistory() {
        return ticketHistory;
    }

    public void setTicketHistory(final TicketHistory ticketHistory) {
        this.ticketHistory = ticketHistory;
    }
}
