package database;

import java.util.Comparator;
import mathutils.MathUtil;
import tickets.Ticket;
import tickets.Ticket.BusinessPriority;
import tickets.Ticket.Status;
import milestones.Milestone;
import io.CommandInput;
import io.IOUtil;
import io.UserInput;
import lombok.Getter;
import lombok.Setter;
import tickets.Ticket.TicketHistory;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import validation.developerhandlers.DeveloperValidationHandler;
import validation.developerhandlers.ExpertiseAreaHandler;
import validation.developerhandlers.LockedMilestoneHandler;
import validation.developerhandlers.MilestoneAssignmentHandler;
import validation.developerhandlers.SeniorityLevelHandler;
import validation.developerhandlers.TicketStatusHandler;
import validation.commenthandlers.CommentValidationHandler;
import validation.commenthandlers.TicketExistenceHandler;
import validation.commenthandlers.AnonymousTicketHandler;
import validation.commenthandlers.ClosedTicketHandler;
import validation.commenthandlers.CommentLengthHandler;
import validation.commenthandlers.DeveloperAssignmentHandler;
import validation.commenthandlers.ReporterOwnershipHandler;
import java.util.Collections;
import users.User;
import users.Manager;
import users.Developer;
import users.Reporter;

import services.AnalyticsService;
import tickets.TicketFactory;
import search.SearchService;

/**
 * Main database class that manages users, tickets, milestones, and commands.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
@Getter
@Setter
public final class Database {
    private static Database instance;
    private static final int UP_PRIORITY_INTERVAL = 3;

    /**
     * Sets the singleton instance of the database.
     *
     * @param instance The database instance to set
     */
    public static void setInstance(final Database instance) {
        Database.instance = instance;
    }

    /**
     * Gets the singleton instance of the database.
     * Creates a new instance if one doesn't exist.
     *
     * @return The singleton database instance
     */
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private final String usersDb = "input/database/users.json";
    private List<User> users = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private List<CommandInput> commands = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private LocalDate lastUpdate;

    /**
     * Private constructor for Singleton pattern.
     */
    private Database() {
    }

    /**
     * Gets the size of a specific collection in the database.
     *
     * @param who The collection to check ("users", "tickets", "commands", or
     *            "milestones")
     * @return The size of the collection, or -1 if invalid collection name
     */
    public int getSize(final String who) {
        switch (who) {
            case "users":
                return users.size();
            case "tickets":
                return tickets.size();
            case "commands":
                return commands.size();
            case "milestones":
                return milestones.size();
            default:
                return -1;
        }
    }

    /**
     * Clears all data from the database.
     */
    public void clearDatabase() {
        users.clear();
        tickets.clear();
        commands.clear();
        milestones.clear();
    }

    /**
     * Gets the milestone name associated with a ticket ID.
     *
     * @param ticketId The ID of the ticket
     * @return The milestone name, or null if not found
     */
    public String getMilestoneNameFromTicketID(final int ticketId) {
        for (final Milestone m : milestones) {
            for (final int id : m.getTickets()) {
                if (id == ticketId) {
                    return m.getName();
                }
            }
        }
        return null;
    }

    /**
     * Gets the milestone associated with a ticket ID.
     *
     * @param ticketId The ID of the ticket
     * @return The milestone object, or null if not found
     */
    public Milestone getMilestoneFromTicketID(final int ticketId) {
        for (final Milestone m : milestones) {
            for (final int id : m.getTickets()) {
                if (id == ticketId) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Assigns a ticket to a developer.
     *
     * @param command The command containing assignment details
     */
    public void assignTicket(final CommandInput command) {
        final Developer dev = (Developer) getUser(command.username());
        final Milestone mstn = getMilestoneFromTicketID(command.ticketID());
        final Ticket tkt = getTicket(command.ticketID());

        if (tkt == null) {
            return;
        }

        final DeveloperValidationHandler validateDev = new ExpertiseAreaHandler();
        validateDev.setNext(new SeniorityLevelHandler())
                .setNext(new TicketStatusHandler())
                .setNext(new MilestoneAssignmentHandler())
                .setNext(new LockedMilestoneHandler());

        final boolean isDevValid = validateDev.check(dev, tkt, mstn, command);

        if (!isDevValid) {
            System.out.println("INVALID ASSIGNMENT");
            return;
        }

        tkt.assignDeveloper(command);
        mstn.assignDeveloper(command);
        dev.assignToTicket(command.ticketID());
    }

    /**
     * Undoes a ticket assignment.
     *
     * @param command The command containing undo assignment details
     */
    public void undoAssignedTicket(final CommandInput command) {
        final Ticket tkt = getTicket(command.ticketID());
        final Milestone milestone = getMilestoneFromTicketID(command.ticketID());
        final Developer dev = (Developer) getUser(command.username());

        if (tkt == null) {
            return;
        }

        dev.deassignFromTicket(command.ticketID());
        tkt.undoAssignDeveloper(command);
        milestone.removeTicketFromDev(command);
    }

    /**
     * Gets a user by username.
     *
     * @param username The username to search for
     * @return The user object, or null if not found
     */
    public User getUser(final String username) {
        for (final User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Gets a ticket by ID.
     *
     * @param id The ticket ID to search for
     * @return The ticket object, or null if not found
     */
    public Ticket getTicket(final int id) {
        for (final Ticket t : tickets) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets all tickets assigned to a specific user.
     *
     * @param username The username to filter by
     * @return List of tickets assigned to the user, sorted by priority and ID
     */
    public List<Ticket> getAssignedTickets(final String username) {
        return tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() != null
                        && ticket.getAssignedTo().equals(username))
                .sorted(Comparator
                        .comparing(Ticket::getBusinessPriority).reversed()
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new milestone to the database.
     *
     * @param command The command containing milestone details
     */
    public void addMilestone(final CommandInput command) {
        if (!command.username().contains("manager")) {
            if (command.username().contains("reporter")) {
                IOUtil.milestoneError(command, "WRONG_USER_REPORTER");
                return;
            }
            IOUtil.milestoneError(command, "WRONG_USER_DEVELOPER");
            return;
        }

        for (final int commandTicketId : command.tickets()) {
            if (getMilestoneNameFromTicketID(commandTicketId) != null) {
                IOUtil.milestoneError(command,
                        "DUPE_" + getMilestoneNameFromTicketID(commandTicketId)
                                + "_" + commandTicketId);
                return;
            }
        }

        milestones.add(new Milestone(command.username(), command.timestamp(),
                command.name(), command.blockingFor(),
                command.dueDate(), command.tickets(), command.assignedDevs()));
        final Milestone milestone = milestones.getLast();
        for (final int ticketId : milestone.getOpenTickets()) {
            final Ticket ticket = getTicket(ticketId);
            if (ticket == null) {
                continue;
            }
            ticket.addActionMilestone(milestone.getName(), milestone.getOwner(),
                    milestone.getCreatedAt());
        }
        for (final String devUsername : milestone.getAssignedDevs()) {
            final User user = getUser(devUsername);
            if (user.getRole().name().equals("DEVELOPER")) {
                final Developer dev = (Developer) user;
                milestone.addObserver(dev);
            }
        }
        milestone.notifyCreated();
    }

    /**
     * Gets a milestone by name.
     *
     * @param name The name of the milestone to find
     * @return The milestone object, or null if not found
     */
    public Milestone getMilestoneFromName(final String name) {
        for (final Milestone milestone : milestones) {
            if (milestone.getName().equals(name)) {
                return milestone;
            }
        }
        return null;
    }

    /**
     * Gets all tickets concerning a specific user.
     *
     * @param username The username to filter by
     * @return List of tickets concerning the user, or null for reporters
     */
    public List<Ticket> getTicketsConcerningUser(final String username) {
        final List<Ticket> filteredTickets = new ArrayList<>();
        final User user = getUser(username);

        if (user.getRole().name().equals("REPORTER")) {
            return null;
        }

        for (final Ticket ticket : tickets) {
            boolean shouldAddTicket = false;

            switch (user.getRole().name()) {
                case "DEVELOPER":
                    if (ticket.getTicketHistory() != null) {
                        for (final Ticket.Action action : ticket.getTicketHistory().getActions()) {
                            if (username.equals(action.getBy())) {
                                shouldAddTicket = true;
                                break;
                            }
                        }
                    }

                    if (!shouldAddTicket && ticket.getComments() != null) {
                        for (final Ticket.Comment comment : ticket.getComments()) {
                            if (username.equals(comment.getAuthor())) {
                                shouldAddTicket = true;
                                break;
                            }
                        }
                    }
                    break;

                case "MANAGER":
                    final List<Milestone> managerMilestones = getMilestonesFromUser(username);

                    for (final Milestone milestone : managerMilestones) {
                        if (milestone.containsTicket(ticket.getId())) {
                            shouldAddTicket = true;
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }

            if (shouldAddTicket) {
                filteredTickets.add(ticket);
            }
        }

        Collections.sort(filteredTickets, (t1, t2) -> {
            final int dateCompare = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (dateCompare != 0) {
                return dateCompare;
            }
            return Integer.compare(t1.getId(), t2.getId());
        });

        return filteredTickets;
    }

    /**
     * Gets all milestones owned by a specific user.
     *
     * @param user The username to filter by
     * @return List of milestones owned by the user
     */
    public List<Milestone> getMilestonesFromUser(final String user) {
        final List<Milestone> userMilestones = new ArrayList<>();

        for (final Milestone milestone : milestones) {
            if (milestone.getOwner().equals(user)) {
                userMilestones.add(milestone);
            }
        }

        return userMilestones;
    }

    /**
     * Blocks a milestone by name.
     *
     * @param name The name of the milestone to block
     */
    public void blockMilestone(final String name) {
        milestones.stream()
                .filter(milestone -> name.equals(milestone.getName()))
                .findFirst()
                .ifPresent(milestone -> milestone.setBlocked(true));
    }

    /**
     * Gets milestones visible to a specific user based on their role.
     *
     * @param username The username to filter by
     * @return List of milestones visible to the user
     */
    public List<Milestone> getMilestones(final String username) {
        final int lUnderscore = username.lastIndexOf('_');
        final String role = username.substring(lUnderscore + 1);
        switch (role) {
            case "manager":
                return milestones.stream()
                        .filter(milestone -> milestone.getOwner().equals(username))
                        .collect(Collectors.toList());
            case "reporter":
                System.out.println("dont think they can see anything");
                return List.of();
            default:
                return milestones.stream()
                        .filter(milestone -> Arrays.stream(milestone.getAssignedDevs())
                                .anyMatch(developer -> developer.equals(username)))
                        .collect(Collectors.toList());
        }
    }

    /**
     * Adds a new ticket to the database.
     *
     * @param command The command containing ticket details
     */
    public void addTicket(final CommandInput command) {
        if (!command.params().type().equals("BUG") && command.params().reportedBy().isEmpty()) {
            IOUtil.ticketError(command, "ANON");
            return;
        }
        if (!command.params().reportedBy().isEmpty() && !userExists(command.username())) {
            IOUtil.ticketError(command, "NUSR");
            return;
        }

        tickets.add(TicketFactory.createTicket(command));
        Ticket.setTicketId(Ticket.getTicketId() + 1);
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param username The username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(final String username) {
        return users.stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    /**
     * Gets the path to the users database file.
     *
     * @return The users database file path
     */
    public String getUsersDb() {
        return usersDb;
    }

    /**
     * Gets all users in the database.
     *
     * @return List of all users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets users from a list of user inputs.
     *
     * @param inputs List of user inputs to create users from
     */
    public void setUsers(final List<UserInput> inputs) {
        users = inputs.stream()
                .map(input -> switch (input.role()) {
                    case "REPORTER" ->
                        new Reporter(input.username(), input.email(), input.role());
                    case "DEVELOPER" ->
                        new Developer(input.username(), input.email(), input.role(),
                                input.hireDate(), input.expertiseArea(), input.seniority());
                    case "MANAGER" ->
                        new Manager(input.username(), input.email(), input.role(),
                                input.hireDate(), input.subordinates());
                    default -> throw new IllegalArgumentException("Unknown role");

                })
                .collect(Collectors.toList());
    }

    /**
     * Gets tickets visible to a specific user based on their role.
     *
     * @param username The username to filter by
     * @return List of tickets visible to the user
     */
    public List<Ticket> getTickets(final String username) {
        final int lUnderscore = username.lastIndexOf('_');
        final String role = username.substring(lUnderscore + 1);
        switch (role) {
            case "manager":
                return new ArrayList<>(tickets);
            case "reporter":
                return tickets.stream()
                        .filter(ticket -> username.equals(ticket.getReportedBy()))
                        .collect(Collectors.toList());
            default:
                final List<Milestone> devMilestones = milestones.stream()
                        .filter(milestone -> milestone.getAssignedDevs() != null
                                && Arrays.stream(milestone.getAssignedDevs())
                                        .anyMatch(dev -> dev.equals(username)))
                        .collect(Collectors.toList());

                final List<Ticket> result = new ArrayList<>();

                for (final Milestone milestone : devMilestones) {
                    for (final int ticketId : milestone.getTickets()) {
                        for (final Ticket ticket : tickets) {
                            if (ticket.getId() == ticketId
                                    && "OPEN".equals(ticket.getStatus().toString())) {
                                result.add(ticket);
                                break;
                            }
                        }
                    }
                }

                return result;

        }
    }

    /**
     * Adds a comment to a ticket.
     *
     * @param command The command containing comment details
     */
    public void addComment(final CommandInput command) {
        final CommentValidationHandler validateComment = new TicketExistenceHandler();
        validateComment.setNext(new AnonymousTicketHandler())
                .setNext(new ClosedTicketHandler())
                .setNext(new CommentLengthHandler())
                .setNext(new DeveloperAssignmentHandler())
                .setNext(new ReporterOwnershipHandler());

        final Ticket ticket = getTicket(command.ticketID());

        final boolean isValid = validateComment.validate(command);
        if (!isValid) {
            return;
        }
        ticket.addComment(command.username(), command.comment(), command.timestamp());

        final Developer developer = (Developer) getUser(ticket.getAssignedTo());
        if (developer != null) {
            developer.incrementCommentCount(ticket.getId());
        }
    }

    /**
     * Undoes adding a comment to a ticket.
     *
     * @param command The command containing undo comment details
     */
    public void undoAddComment(final CommandInput command) {
        final Ticket ticket = getTicket(command.ticketID());

        if (ticket == null) {
            return;
        }
        if (ticket.getReportedBy().isEmpty()) {
            IOUtil.commentError(command, "ANON");
            return;
        }
        ticket.undoAddComment(command.username());
    }

    /**
     * Changes the status of a ticket.
     *
     * @param command The command containing status change details
     */
    public void changeStatus(final CommandInput command) {
        final Ticket ticket = getTicket(command.ticketID());
        final Developer dev = (Developer) getUser(command.username());
        if (ticket == null) {
            return;
        }
        if (ticket.getAssignedTo() == null) {
            return;
        }
        if (!ticket.getAssignedTo().equals(command.username())) {
            IOUtil.changeError(command, "ASSIGNMENT");
            return;
        }

        final Status oldStatus = ticket.getStatus();

        switch (oldStatus.name()) {
            case "IN_PROGRESS":
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(),
                        command.timestamp());
                dev.setClosedTickets(dev.getClosedTickets() + 1);
                break;
            case "RESOLVED":
                ticket.changeStatus(Ticket.Status.CLOSED, command.username(), command.timestamp());
                final Milestone milestone = getMilestoneFromTicketID(command.ticketID());
                milestone.changeStatusOfTicket(command);
                break;
            default:
                break;
        }
    }

    /**
     * Undoes a status change on a ticket.
     *
     * @param command The command containing undo status change details
     */
    public void undoChangeStatus(final CommandInput command) {
        final Ticket ticket = getTicket(command.ticketID());

        if (ticket == null) {
            return;
        }
        if (!ticket.getAssignedTo().equals(command.username())) {
            IOUtil.changeError(command, "ASSIGNMENT");
            return;
        }

        final Status currentStatus = ticket.getStatus();

        switch (currentStatus.name()) {
            case "RESOLVED":
                ticket.changeStatus(Ticket.Status.IN_PROGRESS, command.username(),
                        command.timestamp());
                break;
            case "CLOSED":
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(),
                        command.timestamp());
                ticket.setSolvedAt(null);
                final Milestone milestone = getMilestoneFromTicketID(command.ticketID());
                milestone.undoChangeStatusOfTicket(command);
                break;
            default:
                break;
        }
    }

    /**
     * Gets the history of a ticket.
     *
     * @param id The ID of the ticket
     * @return The ticket history object
     */
    public TicketHistory getTicketHistory(final int id) {
        final Ticket ticket = getTicket(id);
        return ticket.getTicketHistory();
    }

    /**
     * Updates the database to a specific date.
     *
     * @param date The date to update to
     */
    public void update(final LocalDate date) {
        for (LocalDate time = lastUpdate; !time.isAfter(date); time = time.plusDays(1)) {
            miniUpdate(time);
        }
        lastUpdate = date;
    }

    /**
     * Performs a daily update on the database.
     *
     * @param date The date for the update
     */
    public void miniUpdate(final LocalDate date) {
        milestones.forEach(milestone -> {
            final int timeLeft = (int) ChronoUnit.DAYS
                    .between(date, LocalDate.parse(milestone.getDueDate()));

            if (milestone.getCompletionPercentage() == 1.0) {
                milestone.setStatus("COMPLETED");
            }
            if (milestone.getStatus().equals("COMPLETED")) {
                return;
            }

            if (timeLeft < 0) {
                milestone.setDaysUntilDue(0);
                milestone.setOverdueBy(-timeLeft + 1);
            } else {
                milestone.setDaysUntilDue(timeLeft + 1);
                milestone.setOverdueBy(0);
            }

            if (date.equals(lastUpdate)) {
                return;
            }

            int timeSinceCreation;
            if (!milestone.isBlocked()) {
                if (milestone.getUnlockedDate() != null) {
                    timeSinceCreation = Math
                            .abs((int) ChronoUnit.DAYS.between((milestone.getUnlockedDate()),
                                    date));
                } else {
                    timeSinceCreation = Math
                            .abs((int) ChronoUnit.DAYS
                                    .between((LocalDate.parse(milestone.getCreatedAt())),
                                            date));
                }
            } else {
                timeSinceCreation = 0;
            }

            if ((timeSinceCreation != 0) && !milestone.isBlocked()) {
                boolean crit = false;
                if (timeLeft <= 1) {
                    crit = true;
                    if (timeLeft == 1) {
                        milestone.notifyDueTomorrow();
                    }
                }
                for (final int ticketId : milestone.getTickets()) {
                    final Ticket ticket = getTicket(ticketId);
                    if (ticket == null) {
                        continue;
                    }
                    if (ticket.getStatus().name().equals("CLOSED")) {
                        continue;
                    }

                    if (crit) {
                        ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                    }
                    if (timeSinceCreation % UP_PRIORITY_INTERVAL == 0) {
                        ticket.upPriority();
                    }

                    if (ticket.getStatus() == Status.IN_PROGRESS
                            && ticket.getAssignedTo() != null) {
                        User user = getUser(ticket.getAssignedTo());
                        if (user.getRole().name().equals("DEVELOPER")) {
                            Developer dev = (Developer) user;
                            if (!canHandlePriority(dev, ticket.getBusinessPriority())) {
                                dev.deassignFromTicket(ticket.getId());
                                ticket.setAssignedTo(null);
                                ticket.setStatus(Status.OPEN);

                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Checks if a developer can handle a ticket with given priority.
     *
     * @param dev      The developer to check
     * @param priority The priority to check against
     * @return true if the developer can handle the priority, false otherwise
     */
    private boolean canHandlePriority(final Developer dev, final BusinessPriority priority) {
        switch (dev.getSeniority()) {
            case SENIOR:
                return true;
            case MID:
                return priority != BusinessPriority.CRITICAL;
            case JUNIOR:
                return priority == BusinessPriority.LOW || priority == BusinessPriority.MEDIUM;
            default:
                return false;
        }
    }

    /**
     * Gets customer impact analytics data.
     *
     * @return List of customer impact metrics
     */
    public List<Number> getCustomerImpact() {
        return AnalyticsService.getCustomerImpact(this.tickets);
    }

    /**
     * Gets resolution efficiency analytics data.
     *
     * @return List of resolution efficiency metrics
     */
    public List<Number> getResolutionEfficiency() {
        return AnalyticsService.getResolutionEfficiency(this.tickets);
    }

    /**
     * Gets application stability analytics data.
     *
     * @return List of application stability metrics
     */
    public List<Object> getAppStability() {
        return new AnalyticsService().getAppStability();
    }

    /**
     * Gets performance report for a manager's subordinates.
     *
     * @param command The command containing performance report details
     * @return List of performance metrics for each subordinate
     */
    public List<List<Object>> getPerformance(final CommandInput command) {
        final List<List<Object>> report = new ArrayList<>();
        final Manager manager = (Manager) getUser(command.username());

        final List<String> subordinateUsernames = Arrays.asList(manager.getSubordinates());
        final List<String> sortedUsernames = new ArrayList<>(subordinateUsernames);
        Collections.sort(sortedUsernames);

        for (final String username : sortedUsernames) {
            final Developer dev = (Developer) getUser(username);
            if (dev != null) {
                final List<Object> row = new ArrayList<>();

                row.add(username);
                final List<Number> metrics = dev.updatePerformanceScore(command.time());
                row.add(metrics.get(0).intValue());
                row.add(MathUtil.round(metrics.get(2).doubleValue()));
                row.add(MathUtil.round(metrics.get(1).doubleValue()));
                row.add(dev.getSeniority().toString());

                report.add(row);
            }
        }

        return report;
    }

    /**
     * Gets ticket risk analytics data.
     *
     * @return List of ticket risk metrics
     */
    public List<Object> getTicketRisk() {
        return new AnalyticsService().getTicketRisk();
    }

    /**
     * Gets all developers in the database.
     *
     * @return List of all developers
     */
    public List<Developer> getAllDevelopers() {
        final List<Developer> developers = new ArrayList<Developer>();
        for (final User user : users) {
            if (user.getRole().name().equals("DEVELOPER")) {
                developers.add((Developer) user);
            }
        }
        return developers;
    }

    /**
     * Gets search results based on a search command.
     *
     * @param command The command containing search criteria
     * @return List of search results
     */
    public List<?> getSearchResults(final CommandInput command) {
        return SearchService.getSearchResults(command);
    }

    /**
     * Gets all tickets in the database.
     *
     * @return List of all tickets
     */
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

}
