package tickets;

import java.util.ArrayList;
import java.util.List;
import io.CommandInput;

public abstract class Ticket {
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

        public T id(int id) {
            this.id = id;
            return self();
        }

        public T createdAt(String createdAt) {
            this.createdAt = createdAt;
            return self();
        }

        public T type(String type) {
            this.type = type;
            return self();
        }

        public T title(String title) {
            this.title = title;
            return self();
        }

        public T businessPriority(BusinessPriority priority) {
            this.businessPriority = priority;
            return self();
        }

        public T status(Status status) {
            this.status = status;
            return self();
        }

        public T expertiseArea(ExpertiseArea area) {
            this.expertiseArea = area;
            return self();
        }

        public T description(String description) {
            this.description = description;
            return self();
        }

        public T reportedBy(String reportedBy) {
            this.reportedBy = reportedBy;
            return self();
        }

        public abstract Ticket build();

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BusinessPriority getBusinessPriority() {
            return businessPriority;
        }

        public void setBusinessPriority(BusinessPriority businessPriority) {
            this.businessPriority = businessPriority;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public ExpertiseArea getExpertiseArea() {
            return expertiseArea;
        }

        public void setExpertiseArea(ExpertiseArea expertiseArea) {
            this.expertiseArea = expertiseArea;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReportedBy() {
            return reportedBy;
        }

        public void setReportedBy(String reportedBy) {
            this.reportedBy = reportedBy;
        }

        protected abstract T self();
    }

    public class Comment {
        String author;
        String content;
        String createdAt;

        public Comment(String author, String content, String createdAt) {
            this.author = author;
            this.createdAt = createdAt;
            this.content = content;
        }

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
		}
    }

    private static int ticketId = 0;

    public static void clearTicket() {
        ticketId = 0;
    }

    public static int getTicketId() {
        return ticketId;
    }

    public static void setTicketId(int ticketId) {
        Ticket.ticketId = ticketId;
    }

    private int id;
    private String type;
    private String title;
    private String description;
    private String reportedBy;
    private BusinessPriority businessPriority;
    private Status status;
    private String assignedTo;
    private String assignedAt;
    private String solvedAt;
    private String createdAt;
    private ExpertiseArea expertiseArea;
    private List<Comment> comments = new ArrayList<Comment>();

	public Ticket(Builder<?> b) {
        this.id = b.id;
        this.type = b.type;
        this.title = b.title;
        this.businessPriority = b.businessPriority;
        this.status = b.status;
        this.expertiseArea = b.expertiseArea;
        this.description = b.description;
        this.reportedBy = b.reportedBy;
        this.createdAt = b.createdAt;
    }

    public List<Comment> getComments() {
        return comments;
    }

    // TODO imlement the comments part

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void assignDeveloper(CommandInput command) {
        this.setStatus(Status.IN_PROGRESS);
        this.setAssignedTo(command.username());
        this.setAssignedAt(command.timestamp());
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

    public void addComment(String author, String comment, String date) {
        comments.add(new Comment(author, comment, date));
    }

    public void undoAddComment(String author) {
        Comment remove = null;
        for(Comment c : comments) {
            if(c.author.equals(author)){
                remove = c;
            }
        }
        if(remove != null) {
            System.out.println("undid comment");
            comments.remove(remove);
        }
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(String solvedAt) {
        this.solvedAt = solvedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    public void setBusinessPriority(BusinessPriority businessPriority) {
        this.businessPriority = businessPriority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }
}
