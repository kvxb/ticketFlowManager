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
import java.util.Map;
import java.util.HashMap;
import search.filters.FilterContext;
import search.filters.impl.BusinessPriorityFilter;
import search.filters.impl.TypeFilter;
import search.filters.impl.CreatedAtFilter;
import search.filters.impl.CreatedBeforeFilter;
import search.filters.impl.CreatedAfterFilter;
import search.filters.impl.KeywordsFilter;
import search.filters.impl.AvailableForAssignmentFilter;
import search.filters.impl.ExpertiseAreaFilter;
import search.filters.impl.SeniorityFilter;
import search.filters.impl.PerformanceScoreAboveFilter;
import search.filters.impl.PerformanceScoreBelowFilter;
import users.User;
import users.Manager;
import users.Developer;
import users.Reporter;

public class Database {
    private static Database instance;
    private final String USERS_DB = "input/database/users.json";

    private List<User> users = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private List<CommandInput> commands = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private LocalDate lastUpdate;

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

    private List<Milestone> getMilestonesForDeveloper(final String username) {
        final List<Milestone> devMilestones = new ArrayList<>();
        for (final Milestone milestone : milestones) {
            if (Arrays.asList(milestone.getAssignedDevs()).contains(username)) {
                devMilestones.add(milestone);
            }
        }
        return devMilestones;
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
        tickets.add(
                switch (command.params().type()) {
                    case "BUG" -> {
                        final Bug bug = new Bug.Builder()
                                .id(Ticket.getTicketId())
                                .title(command.params().title())
                                .type(command.params().type())
                                .businessPriority(
                                        command.params().reportedBy().isEmpty()
                                                ? Ticket.BusinessPriority.LOW
                                                : Ticket.BusinessPriority.valueOf(
                                                        command.params().businessPriority().toUpperCase()))
                                .expertiseArea(Ticket.ExpertiseArea.valueOf(
                                        command.params().expertiseArea().toUpperCase()))
                                .reportedBy(command.params().reportedBy())
                                .expectedBehaviour(command.params().expectedBehavior())
                                .actualBehaviour(command.params().actualBehavior())
                                .frequency(Bug.Frequency.valueOf(
                                        command.params().frequency().toUpperCase()))
                                .severity(Bug.Severity.valueOf(
                                        command.params().severity().toUpperCase()))
                                .environment(command.params().environment())
                                .errorCode(command.params().errorCode() != null
                                        ? Integer.parseInt(command.params().errorCode())
                                        : 0)
                                .createdAt(command.timestamp())
                                .build();
                        yield bug;
                    }
                    case "FEATURE_REQUEST" -> {
                        final FeatureRequest fr = new FeatureRequest.Builder()
                                .id(Ticket.getTicketId())
                                .type(command.params().type())
                                .title(command.params().title())
                                .businessPriority(Ticket.BusinessPriority.valueOf(
                                        command.params().businessPriority().toUpperCase()))
                                .expertiseArea(Ticket.ExpertiseArea.valueOf(
                                        command.params().expertiseArea().toUpperCase()))
                                .reportedBy(command.params().reportedBy())
                                .businessValue(FeatureRequest.BusinessValue.valueOf(
                                        command.params().businessValue().toUpperCase()))
                                .customerDemand(FeatureRequest.CustomerDemand.valueOf(
                                        command.params().customerDemand().toUpperCase()))
                                .createdAt(command.timestamp())
                                .build();
                        yield fr;
                    }
                    case "UI_FEEDBACK" -> {
                        final UIFeedback ui = new UIFeedback.Builder()
                                .id(Ticket.getTicketId())
                                .type(command.params().type())
                                .title(command.params().title())
                                .businessPriority(Ticket.BusinessPriority.valueOf(
                                        command.params().businessPriority().toUpperCase()))
                                .expertiseArea(Ticket.ExpertiseArea.valueOf(
                                        command.params().expertiseArea().toUpperCase()))
                                .reportedBy(command.params().reportedBy())
                                .businessValue(FeatureRequest.BusinessValue.valueOf(
                                        command.params().businessValue().toUpperCase()))
                                .uiElementId(command.params().uiElementId())
                                .usabilityScore(command.params().usabilityScore())
                                .screenshotUrl(command.params().screenshotUrl())
                                .suggestedFix(command.params().suggestedFix())
                                .createdAt(command.timestamp())
                                .build();
                        yield ui;
                    }
                    default -> throw new IllegalArgumentException(
                            "Unknown ticket type: " + command.params().type());
                });
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
        developer.incrementCommentCount(ticket.getId());
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
                }
            }
        });
    }

    public List<Number> getCustomerImpact() {
        final List<Number> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getImpact();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        report.add(MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        report.add(MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        report.add(MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        return report;
    }

    public List<Number> getResolutionEfficiency() {
        final List<Number> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("OPEN")) {
                continue;
            }
            if (ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getEfficiency();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        report.add(MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        report.add(MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        report.add(MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        return report;
    }

    public List<Object> getAppStability() {
        final List<Object> report = new ArrayList<>();

        int totalOpenTickets = 0;
        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        double bugRiskScore = 0.0;
        double featureRequestRiskScore = 0.0;
        double uiFeedbackRiskScore = 0.0;

        for (final Ticket ticket : tickets) {
            if (!ticket.getStatus().name().equals("OPEN") &&
                    !ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }

            totalOpenTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += ticket.getImpact();
                    bugRiskScore += ticket.getRisk();
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += ticket.getImpact();
                    featureRequestRiskScore += ticket.getRisk();
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += ticket.getImpact();
                    uiFeedbackRiskScore += ticket.getRisk();
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }

        report.add(totalOpenTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        final double avgBugRisk = bugCount > 0 ? bugRiskScore / bugCount : 0;
        final double avgFeatureRisk = featureRequestCount > 0 ? featureRequestRiskScore / featureRequestCount : 0;
        final double avgUIRisk = uiFeedbackCount > 0 ? uiFeedbackRiskScore / uiFeedbackCount : 0;

        final String bugRiskLevel = getRiskLevel(avgBugRisk);
        final String featureRiskLevel = getRiskLevel(avgFeatureRisk);
        final String uiRiskLevel = getRiskLevel(avgUIRisk);

        report.add(bugRiskLevel);
        report.add(featureRiskLevel);
        report.add(uiRiskLevel);

        final double avgBugImpact = bugCount > 0 ? bugImpact / bugCount : 0;
        final double avgFeatureImpact = featureRequestCount > 0 ? featureRequestImpact / featureRequestCount : 0;
        final double avgUIImpact = uiFeedbackCount > 0 ? uiFeedbackImpact / uiFeedbackCount : 0;

        report.add(MathUtil.round(avgBugImpact));
        report.add(MathUtil.round(avgFeatureImpact));
        report.add(MathUtil.round(avgUIImpact));

        final String appStability = determineStability(bugRiskLevel, featureRiskLevel, uiRiskLevel,
                avgBugImpact, avgFeatureImpact, avgUIImpact);
        report.add(appStability);

        return report;
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

    private String determineStability(final String bugRisk, final String featureRisk, final String uiRisk,
            final double bugImpact, final double featureImpact, final double uiImpact) {

        final boolean hasSignificantRisk = bugRisk.equals("SIGNIFICANT") ||
                bugRisk.equals("MAJOR") ||
                featureRisk.equals("SIGNIFICANT") ||
                featureRisk.equals("MAJOR") ||
                uiRisk.equals("SIGNIFICANT") ||
                uiRisk.equals("MAJOR");

        final boolean allNegligible = bugRisk.equals("NEGLIGIBLE") &&
                featureRisk.equals("NEGLIGIBLE") &&
                uiRisk.equals("NEGLIGIBLE");

        final boolean allImpactBelow50 = bugImpact < 50 && featureImpact < 50 && uiImpact < 50;

        if (hasSignificantRisk) {
            return "UNSTABLE";
        }

        if (allNegligible && allImpactBelow50) {
            return "STABLE";
        }

        return "PARTIALLY STABLE";
    }

    public List<Object> getTicketRisk() {
        final List<Object> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getRisk();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        bugImpact = (MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        featureRequestImpact = (MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        uiFeedbackImpact = (MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        final String bugRiskLevel = getRiskLevel(bugImpact);
        final String featureRequestRiskLevel = getRiskLevel(featureRequestImpact);
        final String uiFeedbackRiskLevel = getRiskLevel(uiFeedbackImpact);

        report.add(bugRiskLevel);
        report.add(featureRequestRiskLevel);
        report.add(uiFeedbackRiskLevel);

        return report;
    }

    private String getRiskLevel(final double impact) {
        if (impact >= 0 && impact <= 24)
            return "NEGLIGIBLE";
        if (impact >= 25 && impact <= 49)
            return "MODERATE";
        if (impact >= 50 && impact <= 74)
            return "SIGNIFICANT";
        if (impact >= 75 && impact <= 100)
            return "MAJOR";
        return "NEGLIGIBLE";
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
        final User user = getUser(command.username());
        final FiltersInput filters = command.filters();
        final String searchType = filters.searchType();

        if ("DEVELOPER".equals(searchType)) {
            if (!"MANAGER".equals(user.getRole().name())) {
                return new ArrayList<>();
            }

            final List<Developer> allDevelopers = getAllDevelopers();
            return filterDevelopers((Manager) user, allDevelopers, filters);

        } else {
            final List<Ticket> allTickets = getAllTickets();
            return filterTickets(user, allTickets, filters);
        }
    }

    private List<Ticket> filterTickets(final User user, final List<Ticket> allTickets, final FiltersInput filters) {
        final List<Ticket> accessibleTickets = new ArrayList<>();

        if ("MANAGER".equals(user.getRole().name())) {
            accessibleTickets.addAll(allTickets);
        } else if ("DEVELOPER".equals(user.getRole().name())) {
            final Developer dev = (Developer) user;
            final List<Milestone> devMilestones = getMilestonesForDeveloper(dev.getUsername());

            for (final Milestone milestone : devMilestones) {
                for (final int ticketId : milestone.getTickets()) {
                    for (final Ticket ticket : allTickets) {
                        if (ticket.getId() == ticketId &&
                                ticket.getStatus() == Ticket.Status.OPEN) {

                            boolean alreadyAdded = false;
                            for (final Ticket addedTicket : accessibleTickets) {
                                if (addedTicket.getId() == ticketId) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }

                            if (!alreadyAdded) {
                                accessibleTickets.add(ticket);
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            return new ArrayList<>();
        }

        final FilterContext<Ticket> context = new FilterContext<>();

        context.addStrategy("businessPriority", new BusinessPriorityFilter());
        context.addStrategy("type", new TypeFilter());
        context.addStrategy("createdAt", new CreatedAtFilter());
        context.addStrategy("createdBefore", new CreatedBeforeFilter());
        context.addStrategy("createdAfter", new CreatedAfterFilter());
        context.addStrategy("keywords", new KeywordsFilter());

        if ("DEVELOPER".equals(user.getRole().name())) {
            final Developer dev = (Developer) user;
            context.addStrategy("availableForAssignment", new AvailableForAssignmentFilter(dev));
        }

        final Map<String, String> filterMap = convertToMap(filters);
        final List<Ticket> filtered = context.applyFilters(accessibleTickets, filterMap);

        filtered.sort(Comparator
                .comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        return filtered;
    }

    private List<Developer> filterDevelopers(final Manager manager, final List<Developer> allDevelopers,
            final FiltersInput filters) {
        final List<Developer> subordinates = new ArrayList<>();
        final List<String> subordinateUsernames = Arrays.asList(manager.getSubordinates());

        for (final Developer dev : allDevelopers) {
            if (subordinateUsernames.contains(dev.getUsername())) {
                subordinates.add(dev);
            }
        }

        final FilterContext<Developer> context = new FilterContext<>();

        context.addStrategy("expertiseArea", new ExpertiseAreaFilter());
        context.addStrategy("seniority", new SeniorityFilter());
        context.addStrategy("performanceScoreAbove", new PerformanceScoreAboveFilter());
        context.addStrategy("performanceScoreBelow", new PerformanceScoreBelowFilter());

        final Map<String, String> filterMap = convertToMap(filters);
        final List<Developer> filtered = context.applyFilters(subordinates, filterMap);

        filtered.sort(Comparator.comparing(Developer::getUsername));

        return filtered;
    }

    private Map<String, String> convertToMap(final FiltersInput filters) {
        final Map<String, String> map = new HashMap<>();

        if (filters.businessPriority() != null) {
            map.put("businessPriority", filters.businessPriority());
        }
        if (filters.type() != null) {
            map.put("type", filters.type());
        }
        if (filters.createdAt() != null) {
            map.put("createdAt", filters.createdAt());
        }
        if (filters.createdBefore() != null) {
            map.put("createdBefore", filters.createdBefore());
        }
        if (filters.createdAfter() != null) {
            map.put("createdAfter", filters.createdAfter());
        }
        if (filters.availableForAssignment() != null) {
            map.put("availableForAssignment", String.valueOf(filters.availableForAssignment()));
        }
        if (filters.keywords() != null && filters.keywords().length > 0) {
            map.put("keywords", Arrays.toString(filters.keywords()));
        }
        if (filters.expertiseArea() != null) {
            map.put("expertiseArea", filters.expertiseArea());
        }
        if (filters.seniority() != null) {
            map.put("seniority", filters.seniority());
        }
        if (filters.performanceScoreAbove() > 0) {
            map.put("performanceScoreAbove", String.valueOf(filters.performanceScoreAbove()));
        }
        if (filters.performanceScoreBelow() > 0) {
            map.put("performanceScoreBelow", String.valueOf(filters.performanceScoreBelow()));
        }

        return map;
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

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
}
