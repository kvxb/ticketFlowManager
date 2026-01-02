package tickets;


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

    private int id;
    private String type;
    private String title;
    private String description;
    private String reportedBy;
    private BusinessPriority businessPriority;
    private Status status;
    private ExpertiseArea expertiseArea;

    public Ticket(Builder<?> b) {
        this.id = b.id;
        this.type = b.type;
        this.title = b.title;
        this.businessPriority = b.businessPriority;
        this.status = b.status;
        this.expertiseArea = b.expertiseArea;
        this.description = b.description;
        this.reportedBy = b.reportedBy;
    }

    public abstract static class Builder<T extends Builder<T>> {
        protected int id;
        protected String type;
        protected String title;
        protected BusinessPriority businessPriority;
        protected Status status = Status.OPEN;
        protected ExpertiseArea expertiseArea;
        protected String description;
        protected String reportedBy;

        public T id(int id) {
            this.id = id;
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

        
        protected abstract T self();

        public abstract Ticket build();
    }
}
