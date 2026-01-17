package milestones;

import java.util.Arrays;
import database.Database;
import io.CommandInput;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;
import mathutils.MathUtil;
import notifications.Subject;
import notifications.Observer;
import java.time.LocalDate;

@Data
public class Milestone implements Subject {
    private Database db = Database.getInstance();
    private List<Observer> observers = new ArrayList<>();
    private int timeAlive;

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private boolean sentNotificationDueTomorrow = false;

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void notifyCreated() {
        String message = "New milestone " + this.name +
                " has been created with due date " + this.dueDate + ".";
        notifyObservers(message);
    }

    public void notifyDueTomorrow() {
        if (sentNotificationDueTomorrow) {
            return;
        }
        String message = "Milestone " + this.name +
                " is due tomorrow. All unresolved tickets are now CRITICAL.";
        notifyObservers(message);
        sentNotificationDueTomorrow = true;
    }

    public void notifyUnblocked(int ticketId) {
        String message = "Milestone " + this.name +
                " is now unblocked as ticket " + ticketId + " has been CLOSED.";
        notifyObservers(message);
    }

    public void notifyUnblockedAfterDue() {
        String message = "Milestone " + this.name +
                " was unblocked after due date. All active tickets are now CRITICAL.";
        notifyObservers(message);
    }

    public void removeTicketFromDev(CommandInput command) {
        int ticketId = command.ticketID();
        String username = command.username();

        for (Repartition rep : this.getRepartitions()) {
            if (rep.getDev() != null && rep.getDev().equals(username)) {
                List<Integer> assignedTickets = rep.getAssignedTickets();
                if (assignedTickets != null) {
                    assignedTickets.removeIf(id -> id == ticketId);
                }
                break;
            }
        }

        // if (!openTickets.contains(ticketId)) {
        // openTickets.add(ticketId);
        // }
        //
        // closedTickets.removeIf(id -> id == ticketId);
        //
        // this.updateCompletionPercentage(command.time());
    }

    @Data
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

    }

    public void assignDeveloper(CommandInput command) {
        for (Repartition rep : this.getRepartitions()) {
            if (rep.getDev().equals(command.username())) {
                rep.getAssignedTickets().add(command.ticketID());
                break;
            }
        }
    }

    private int lastTicket = -1;

    public void changeStatusOfTicket(CommandInput command) {
        int id = command.ticketID();
        if (openTickets.contains(id)) {
            openTickets.remove(Integer.valueOf(id));
            closedTickets.add(id);
        }
        if (openTickets.isEmpty()) {
            lastTicket = id;
        }
        this.updateCompletionPercentage(command.time());
    }

    public void undoChangeStatusOfTicket(CommandInput command) {
        int id = command.ticketID();
        if (closedTickets.contains(id)) {
            openTickets.add(id);
            closedTickets.remove(Integer.valueOf(id));
        }
        this.updateCompletionPercentage(command.time());
    }

    private LocalDate unlockedDate;

    // TODO: this is runnign on assumption that a milestone cant be blocke by two
    // other at the same time which is wrong fix later
    public void updateCompletionPercentage(LocalDate time) {
        this.completionPercentage = MathUtil.round(getNumberOfTickets("CLOSED") / getNumberOfTickets("ALL"));
        if (completionPercentage == 1.0) {
            for (String milestoneName : blockingFor) {
                Milestone blockedMilestone = db.getMilestoneFromName(milestoneName);
                blockedMilestone.setBlocked(false);
                blockedMilestone.setUnlockedDate(time);

                // TODO dont we already have overdueBy and stuff like that for this ?
                // LocalDate dueDate = LocalDate.parse(blockedMilestone.getDueDate());
                // LocalDate currentDate = LocalDate.parse(this.createdAt);

                if (this.overdueBy > 0) {
                    blockedMilestone.notifyUnblockedAfterDue();
                } else {
                    // TODO keep the last ticket to be resolved;
                    blockedMilestone.notifyUnblocked(lastTicket);
                }
            }
        }
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
                    .forEach(milestone -> db.blockMilestone(milestone));
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
}
