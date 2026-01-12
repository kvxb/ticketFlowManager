package milestones;

import java.util.Arrays;
import database.Database;
import io.CommandInput;
import java.util.List;
import java.util.ArrayList;
import mathutils.MathUtil;

public class Milestone {
    public class Repartition {
        private String dev;
        private List<Integer> assignedTickets;

        public Repartition() {
            assignedTickets = new ArrayList<>();
        }

        public Repartition(String name) {
            assignedTickets = new ArrayList<>();
            this.dev = name;
        }

        public Repartition(String name, List<Integer> assignedTickets) {
            this.dev = name;
            this.assignedTickets = (assignedTickets != null) ? assignedTickets : new ArrayList<>();
        }

        public String getDev() {
            return dev;
        }

        public void setDev(String dev) {
            this.dev = dev;
        }

        public List<Integer> getAssignedTickets() {
            return assignedTickets;
        }

        public void setAssignedTickets(List<Integer> assignedTickets) {
            this.assignedTickets = assignedTickets;
        }
    }

    public void assignDeveloper(CommandInput command) {
        for (Repartition rep : this.getRepartitions()) {
            if (rep.getDev().equals(command.username())) {
                rep.getAssignedTickets().add(command.ticketID());
                break;
            }
        }
    }

    public void changeStatusOfTicket(int id) {
        if (openTickets.contains(id)) {
            openTickets.remove(Integer.valueOf(id));
            closedTickets.add(id);
        }
        this.updateCompletionPercentage();
    }

    public void undoChangeStatusOfTicket(int id) {
        if (closedTickets.contains(id)) {
            openTickets.add(id);
            closedTickets.remove(Integer.valueOf(id));
        }
        this.updateCompletionPercentage();
    }

    public void updateCompletionPercentage() {
        this.completionPercentage = MathUtil.round(getNumberOfTickets("CLOSED") / getNumberOfTickets("ALL"));
    }

    public double getNumberOfTickets(String typeOf) {
        switch (typeOf) {
            case "OPEN":
                return (double) openTickets.size();
            case "CLOSED":
                return (double) closedTickets.size();
            case "ALL":
                return (double) (openTickets.size() + closedTickets.size());
            default:
                return 0;
        }
    }

    private String name;
    private String[] blockingFor;
    private String dueDate;
    private int[] tickets;
    private List<Integer> openTickets = new ArrayList<>();
    private List<Integer> closedTickets = new ArrayList<>();
    private double completionPercentage;
    private String[] assignedDevs;
    private String status;
    private boolean isBlocked;

    private int overdueBy;
    private int daysUntilDue;
    private Repartition[] repartitions;

    private String createdAt;
    private String owner;

    public Milestone(String owner, String createdAt, String name, String[] blockingFor, String dueDate, int[] tickets,
            String[] assignedDevs) {
        this.owner = owner;
        this.createdAt = createdAt;
        this.name = name;
        this.blockingFor = blockingFor;
        if (blockingFor != null) {
            Arrays.stream(blockingFor)
                    .forEach(milestone -> Database.blockMilestone(milestone));
        }
        this.dueDate = dueDate;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
        this.status = "ACTIVE";

        for (int ticketId : tickets) {
            openTickets.add(ticketId);
        }

        if (assignedDevs == null || assignedDevs.length == 0) {
            this.repartitions = new Repartition[0];
        } else {
            this.repartitions = new Repartition[assignedDevs.length];
            for (int i = 0; i < assignedDevs.length; i++) {
                this.repartitions[i] = new Repartition(assignedDevs[i]);
            }
        }
    }

    public boolean containsTicket(int id) {
        for (int ticketId : tickets) {
            if (ticketId == id) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDeveloper(String username) {
        return Arrays.stream(this.assignedDevs)
                .anyMatch(name -> name.equals(username));
    }

    public int getDaysUntilDue() {
        return daysUntilDue;
    }

    public void setDaysUntilDue(int daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getBlockingFor() {
        return blockingFor;
    }

    public void setBlockingFor(String[] blockingFor) {
        this.blockingFor = blockingFor;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int[] getTickets() {
        return tickets;
    }

    public void setTickets(int[] tickets) {
        this.tickets = tickets;
    }

    public String[] getAssignedDevs() {
        return assignedDevs;
    }

    public void setAssignedDevs(String[] assignedDevs) {
        this.assignedDevs = assignedDevs;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Integer> getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(List<Integer> openTickets) {
        this.openTickets = openTickets;
    }

    public List<Integer> getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(List<Integer> closedTickets) {
        this.closedTickets = closedTickets;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public int getOverdueBy() {
        return overdueBy;
    }

    public void setOverdueBy(int overdueBy) {
        this.overdueBy = overdueBy;
    }

    public Repartition[] getRepartitions() {
        return repartitions;
    }

    public void setRepartitions(Repartition[] repartitions) {
        this.repartitions = repartitions;
    }
}
