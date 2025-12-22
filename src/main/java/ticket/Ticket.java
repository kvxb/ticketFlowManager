package ticket;

public class Ticket{
    public enum businessPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    public enum status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }
    public enum expertiseArea {
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
}
