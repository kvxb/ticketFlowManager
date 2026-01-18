package milestones;

import java.util.Arrays;
import database.Database;
import io.CommandInput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
import mathutils.MathUtil;
import notifications.Subject;
import notifications.Observer;
import java.time.LocalDate;

/**
 * Represents a milestone in the project management system.
 * Manages tickets, deadlines, and notifies observers (developers) about
 * changes.
 */
@Getter
@Setter
public final class Milestone implements Subject {
    private final Database db = Database.getInstance();
    private List<Observer> observers = new ArrayList<>();
    private boolean sentNotificationDueTomorrow = false;

    private int lastTicket = -1;
    private LocalDate unlockedDate;

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

    /**
     * Constructs a new Milestone.
     *
     * @param owner        The username of the creator.
     * @param createdAt    The creation timestamp.
     * @param name         The name of the milestone.
     * @param blockingFor  Array of milestone names this milestone blocks.
     * @param dueDate      The due date string.
     * @param tickets      Array of ticket IDs included in this milestone.
     * @param assignedDevs Array of developer usernames assigned to this milestone.
     */
    public Milestone(final String owner, final String createdAt, final String name,
            final String[] blockingFor, final String dueDate, final int[] tickets,
            final String[] assignedDevs) {
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

    @Override
    public void addObserver(final Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(final Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(final String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    /**
     * Notifies observers that the milestone has been created.
     */
    public void notifyCreated() {
        String message = "New milestone " + this.name
                + " has been created with due date " + this.dueDate + ".";
        notifyObservers(message);
    }

    /**
     * Notifies observers that the milestone is due tomorrow.
     * Only sends the notification once.
     */
    public void notifyDueTomorrow() {
        if (sentNotificationDueTomorrow) {
            return;
        }
        String message = "Milestone " + this.name
                + " is due tomorrow. All unresolved tickets are now CRITICAL.";
        notifyObservers(message);
        sentNotificationDueTomorrow = true;
    }

    /**
     * Notifies observers that the milestone is unblocked.
     *
     * @param ticketId The ID of the ticket that caused the unblocking.
     */
    public void notifyUnblocked(final int ticketId) {
        String message = "Milestone " + this.name
                + " is now unblocked as ticket " + ticketId + " has been CLOSED.";
        notifyObservers(message);
    }

    /**
     * Notifies observers that the milestone was unblocked after its due date.
     */
    public void notifyUnblockedAfterDue() {
        String message = "Milestone " + this.name
                + " was unblocked after due date. All active tickets are now CRITICAL.";
        notifyObservers(message);
    }

    /**
     * Removes a ticket assignment from a developer in this milestone.
     *
     * @param command The command containing ticket ID and username.
     */
    public void removeTicketFromDev(final CommandInput command) {
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
    }

    /**
     * Inner class representing the repartition of tickets among developers.
     */
    @Getter
    @Setter
    public class Repartition {
        private String dev;
        private List<Integer> assignedTickets;

        /**
         * Default constructor.
         */
        public Repartition() {
            assignedTickets = new ArrayList<>();
        }

        /**
         * Constructor with developer name.
         *
         * @param name The developer's username.
         */
        public Repartition(final String name) {
            assignedTickets = new ArrayList<>();
            this.dev = name;
        }

        /**
         * Constructor with developer name and assigned tickets.
         *
         * @param name            The developer's username.
         * @param assignedTickets List of ticket IDs.
         */
        public Repartition(final String name, final List<Integer> assignedTickets) {
            this.dev = name;
            this.assignedTickets = (assignedTickets != null) ? assignedTickets : new ArrayList<>();
        }
    }

    /**
     * Assigns a developer to a ticket within this milestone.
     *
     * @param command The command containing ticket ID and username.
     */
    public void assignDeveloper(final CommandInput command) {
        for (Repartition rep : this.getRepartitions()) {
            if (rep.getDev().equals(command.username())) {
                rep.getAssignedTickets().add(command.ticketID());
                break;
            }
        }
    }

    /**
     * Updates status when a ticket is closed.
     *
     * @param command The command containing the ticket ID and timestamp.
     */
    public void changeStatusOfTicket(final CommandInput command) {
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

    /**
     * Reverts status change when a ticket is reopened.
     *
     * @param command The command containing the ticket ID and timestamp.
     */
    public void undoChangeStatusOfTicket(final CommandInput command) {
        int id = command.ticketID();
        if (closedTickets.contains(id)) {
            openTickets.add(id);
            closedTickets.remove(Integer.valueOf(id));
        }
        this.updateCompletionPercentage(command.time());
    }

    /**
     * Updates the completion percentage of the milestone.
     * Checks if the milestone is completed and unblocks dependent milestones.
     *
     * @param time The current date.
     */
    // TODO this is runnign on assumption that a milestone cant be blocked by two
    // other at the same time which is wrong fix later
    public void updateCompletionPercentage(final LocalDate time) {
        this.completionPercentage = MathUtil.round(getNumberOfTickets("CLOSED")
                / getNumberOfTickets("ALL"));
        if (completionPercentage == 1.0) {
            for (String milestoneName : blockingFor) {
                Milestone blockedMilestone = db.getMilestoneFromName(milestoneName);
                blockedMilestone.setBlocked(false);
                blockedMilestone.setUnlockedDate(time);

                if (this.overdueBy > 0) {
                    blockedMilestone.notifyUnblockedAfterDue();
                } else {
                    blockedMilestone.notifyUnblocked(lastTicket);
                }
            }
        }
    }

    /**
     * Gets the number of tickets based on their status.
     *
     * @param typeOf The type of tickets to count ("OPEN", "CLOSED", "ALL").
     * @return The count of tickets.
     */
    public double getNumberOfTickets(final String typeOf) {
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

    /**
     * Checks if the milestone contains a specific ticket.
     *
     * @param id The ID of the ticket.
     * @return true if the ticket is in the milestone, false otherwise.
     */
    public boolean containsTicket(final int id) {
        for (int ticketId : tickets) {
            if (ticketId == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a developer is assigned to this milestone.
     *
     * @param username The username of the developer.
     * @return true if the developer is assigned, false otherwise.
     */
    public boolean hasDeveloper(final String username) {
        return Arrays.stream(this.assignedDevs)
                .anyMatch(devName -> devName.equals(username));
    }
}
