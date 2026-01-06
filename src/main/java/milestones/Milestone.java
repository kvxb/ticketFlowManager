package milestones;

import java.time.LocalDate;

public class Milestone {
    private String name;
    private String[] blockingFor;
    private LocalDate dueDate;
    private int[] tickets;
    private String[] assignedDevs;
    private LocalDate createdAt;
    // dont know if this field will be needed but might aswell;
    private String owner;

    public Milestone(String owner, String createdAt, String name, String[] blockingFor, String dueDate, int[] tickets, String[] assignedDevs){
        this.owner = owner;
        this.createdAt = LocalDate.parse(createdAt);
        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = LocalDate.parse(dueDate);
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
    }
}
