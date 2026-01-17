package database;

import tickets.Bug;
import java.util.Comparator;
import tickets.FeatureRequest;
import mathutils.MathUtil;
import tickets.Ticket;
import io.FiltersInput;
import tickets.UIFeedback;
import tickets.Ticket.BusinessPriority;
import tickets.Ticket.Status;
import milestones.Milestone;
import io.CommandInput;
import io.IOUtil;
import io.UserInput;
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

public class Database {
    private static Database instance;

    public static void setInstance(Database instance) {
        Database.instance = instance;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private final String USERS_DB = "input/database/users.json";
    private List<User> users = new ArrayList<>();

    private List<Ticket> tickets = new ArrayList<>();

    private List<CommandInput> commands = new ArrayList<>();

    private final List<Milestone> milestones = new ArrayList<>();

    private LocalDate lastUpdate;

    private Database() {
    }

    public String getUSERS_DB() {
        return USERS_DB;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final LocalDate lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

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

    public void clearDatabase() {
        users.clear();
        tickets.clear();
        commands.clear();
        milestones.clear();
    }

    public String getMilestoneNameFromTicketID(final int TicketID) {
        for (final Milestone m : milestones) {
            for (final int id : m.getTickets()) {
                if (id == TicketID) {
                    return m.getName();
                }
            }
        }
        return null;
    }

    public Milestone getMilestoneFromTicketID(final int TicketID) {
        for (final Milestone m : milestones) {
            for (final int id : m.getTickets()) {
                if (id == TicketID) {
                    return m;
                }
            }
        }
        return null;
    }

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

    public User getUser(final String username) {
        for (final User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public Ticket getTicket(final int id) {
        for (final Ticket t : tickets) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getAssignedTickets(final String username) {
        return tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() != null &&
                        ticket.getAssignedTo().equals(username))
                .sorted(Comparator
                        .comparing(Ticket::getBusinessPriority).reversed()
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());
    }

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
                        "DUPE_" + getMilestoneNameFromTicketID(commandTicketId) + "_" + commandTicketId);
                return;
            }
        }

        milestones.add(new Milestone(command.username(), command.timestamp(), command.name(), command.blockingFor(),
                command.dueDate(), command.tickets(), command.assignedDevs()));
        final Milestone milestone = milestones.getLast();
        for (final int ticketId : milestone.getOpenTickets()) {
            final Ticket ticket = getTicket(ticketId);
            if (ticket == null)
                continue;
            ticket.addActionMilestone(milestone.getName(), milestone.getOwner(), milestone.getCreatedAt());
        }
        for (final String devUsername : milestone.getAssignedDevs()) {
            final User user = getUser(devUsername);
            if (user instanceof Developer) {
                final Developer dev = (Developer) user;
                milestone.addObserver(dev);
            }
        }
        milestone.notifyCreated();
    }

    public Milestone getMilestoneFromName(final String name) {
        for (final Milestone milestone : milestones) {
            if (milestone.getName().equals(name)) {
                return milestone;
            }
        }
        return null;
    }

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
            }

            if (shouldAddTicket) {
                filteredTickets.add(ticket);
            }
        }

        Collections.sort(filteredTickets, (t1, t2) -> {
            final int dateCompare = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (dateCompare != 0)
                return dateCompare;
            return Integer.compare(t1.getId(), t2.getId());
        });

        return filteredTickets;
    }

    public List<Milestone> getMilestonesFromUser(final String user) {
        final List<Milestone> userMilestones = new ArrayList<>();

        for (final Milestone milestone : milestones) {
            if (milestone.getOwner().equals(user)) {
                userMilestones.add(milestone);
            }
        }

        return userMilestones;
    }

    public void blockMilestone(final String name) {
        milestones.stream()
                .filter(milestone -> name.equals(milestone.getName()))
                .findFirst()
                .ifPresent(milestone -> milestone.setBlocked(true));
    }

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

    public boolean userExists(final String username) {
        return users.stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    public String getUsersDb() {
        return USERS_DB;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<UserInput> inputs) {
        users = inputs.stream()
                .map(input -> switch (input.role()) {
                    case "REPORTER" -> new Reporter(input.username(), input.email(), input.role());
                    case "DEVELOPER" -> new Developer(input.username(), input.email(), input.role(), input.hireDate(),
                            input.expertiseArea(), input.seniority());
                    case "MANAGER" -> new Manager(input.username(), input.email(), input.role(), input.hireDate(),
                            input.subordinates());
                    default -> throw new IllegalArgumentException("Unknown role");

                })
                .collect(Collectors.toList());
    }

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
                        .filter(milestone -> milestone.getAssignedDevs() != null &&
                                Arrays.stream(milestone.getAssignedDevs())
                                        .anyMatch(dev -> dev.equals(username)))
                        .collect(Collectors.toList());

                final List<Ticket> result = new ArrayList<>();

                for (final Milestone milestone : devMilestones) {
                    for (final int ticketId : milestone.getTickets()) {
                        for (final Ticket ticket : tickets) {
                            if (ticket.getId() == ticketId && "OPEN".equals(ticket.getStatus().toString())) {
                                result.add(ticket);
                                break;
                            }
                        }
                    }
                }

                return result;

        }
    }

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
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(), command.timestamp());
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
                ticket.changeStatus(Ticket.Status.IN_PROGRESS, command.username(), command.timestamp());
                break;
            case "CLOSED":
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(), command.timestamp());
                ticket.setSolvedAt(null);
                final Milestone milestone = getMilestoneFromTicketID(command.ticketID());
                milestone.undoChangeStatusOfTicket(command);
                break;
            default:
                break;
        }
    }

    public TicketHistory getTicketHistory(final int id) {
        final Ticket ticket = getTicket(id);
        return ticket.getTicketHistory();
    }

    public void update(final LocalDate date) {
        for (LocalDate time = lastUpdate; !time.isAfter(date); time = time.plusDays(1)) {
            miniUpdate(time);
        }
        lastUpdate = date;
    }

    public void miniUpdate(final LocalDate date) {
        milestones.forEach(milestone -> {
            final int timeLeft = (int) ChronoUnit.DAYS.between(date, LocalDate.parse(milestone.getDueDate()));

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

            int timeSinceCreation = 0;
            if (!milestone.isBlocked()) {
                if (milestone.getUnlockedDate() != null) {
                    timeSinceCreation = Math
                            .abs((int) ChronoUnit.DAYS.between((milestone.getUnlockedDate()),
                                    date));
                } else {
                    timeSinceCreation = Math
                            .abs((int) ChronoUnit.DAYS.between((LocalDate.parse(milestone.getCreatedAt())),
                                    date));
                }
            }

            if ((timeSinceCreation != 0) && !milestone.isBlocked()) {
                boolean CRIT = false;
                if (timeLeft <= 1) {
                    CRIT = true;
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

                    if (CRIT) {
                        ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                    }
                    if (timeSinceCreation % 3 == 0) {
                        ticket.upPriority();
                    }

                    if (ticket.getStatus() == Status.IN_PROGRESS && ticket.getAssignedTo() != null) {
                        User user = getUser(ticket.getAssignedTo());
                        if (user instanceof Developer) {
                            Developer dev = (Developer) user;
                            if (!canHandlePriority(dev, ticket.getBusinessPriority())) {
                                dev.deassignFromTicket(ticket.getId());
                                ticket.setAssignedTo(null);
                                ticket.setStatus(Status.OPEN);

                                if (ticket.getTicketHistory() != null) {
                                }
                            }
                        }
                    }
                }
            }
        });
    }

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

    public List<Number> getCustomerImpact() {
        return AnalyticsService.getCustomerImpact(this.tickets);
    }

    public List<Number> getResolutionEfficiency() {
        return AnalyticsService.getResolutionEfficiency(this.tickets);
    }

    public List<Object> getAppStability() {
        return new AnalyticsService().getAppStability();
    }

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

    public List<Object> getTicketRisk() {
        return new AnalyticsService().getTicketRisk();
    }

    public List<Developer> getAllDevelopers() {
        final List<Developer> developers = new ArrayList<Developer>();
        for (final User user : users) {
            if (user.getRole().name().equals("DEVELOPER")) {
                developers.add((Developer) user);
            }
        }
        return developers;
    }

    public List<?> getSearchResults(final CommandInput command) {
        return SearchService.getSearchResults(command);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public void setTickets(final List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<CommandInput> getCommands() {
        return commands;
    }

    public void setCommands(final List<CommandInput> commands) {
        this.commands = commands;
    }
}
