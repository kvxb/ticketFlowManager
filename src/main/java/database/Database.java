package database;

//TODO make this a singleton :(
import tickets.Bug;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
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
import milestones.Milestone.Repartition;
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

    private List<User> users = new ArrayList<>(); // daken from the db file
    private List<Ticket> tickets = new ArrayList<>(); // input in testing period
    private List<CommandInput> commands = new ArrayList<>();
    private List<Milestone> milestones = new ArrayList<>();

    public int getSize(String who) {
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
        // System.out.println("cleared all");
    }

    public String getMilestoneNameFromTicketID(int TicketID) {
        for (Milestone m : milestones) {
            for (int id : m.getTickets()) {
                if (id == TicketID) {
                    return m.getName();
                }
            }
        }
        return null;
    }

    public Milestone getMilestoneFromTicketID(int TicketID) {
        for (Milestone m : milestones) {
            for (int id : m.getTickets()) {
                if (id == TicketID) {
                    return m;
                }
            }
        }
        return null;
    }

    public void assignTicket(CommandInput command) {
        Developer dev = (Developer) getUser(command.username());
        Milestone mstn = getMilestoneFromTicketID(command.ticketID());
        Ticket tkt = getTicket(command.ticketID());

        DeveloperValidationHandler validateDev = new ExpertiseAreaHandler();
        validateDev.setNext(new SeniorityLevelHandler())
                .setNext(new TicketStatusHandler())
                .setNext(new MilestoneAssignmentHandler())
                .setNext(new LockedMilestoneHandler());

        boolean isDevValid = validateDev.check(dev, tkt, mstn, command);

        if (!isDevValid) {
            System.out.println("INVALID ASSIGNMENT");
            return;
        }

        tkt.assignDeveloper(command);
        mstn.assignDeveloper(command);
    }

    public void undoAssignedTicket(CommandInput command) {
        Ticket tkt = tickets.stream()
                .filter(ticket -> ticket.getId() == command.ticketID())
                .findFirst()
                .orElse(null);

        if (tkt == null) {
            System.out.println("didnt find ticket");
            return;
            // TODO: add error here
        }

        tkt.undoAssignDeveloper(command);
        // TODO: is a milestone update not needed here ???? it for sure is
        // but everything at its time
        System.out.println("finished with ticket" + tkt.getId());
    }

    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public Ticket getTicket(int id) {
        for (Ticket t : tickets) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getAssignedTickets(String username) {
        return tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() != null &&
                        ticket.getAssignedTo().equals(username))
                .sorted(Comparator
                        .comparing(Ticket::getBusinessPriority).reversed()
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());
    }

    public void addMilestone(CommandInput command) {
        if (!command.username().contains("manager")) {
            if (command.username().contains("reporter")) {
                IOUtil.milestoneError(command, "WRONG_USER_REPORTER");
                return;
            }
            IOUtil.milestoneError(command, "WRONG_USER_DEVELOPER");
            return;
        }

        for (int commandTicketId : command.tickets()) {
            if (getMilestoneNameFromTicketID(commandTicketId) != null) {
                IOUtil.milestoneError(command,
                        "DUPE_" + getMilestoneNameFromTicketID(commandTicketId) + "_" + commandTicketId);
                return;
            }
        }

        milestones.add(new Milestone(command.username(), command.timestamp(), command.name(), command.blockingFor(),
                command.dueDate(), command.tickets(), command.assignedDevs()));
        Milestone milestone = milestones.getLast();
        for (int ticketId : milestone.getOpenTickets()) {
            Ticket ticket = getTicket(ticketId);
            ticket.addActionMilestone(milestone.getName(), milestone.getOwner(), milestone.getCreatedAt());
        }
        for (String devUsername : milestone.getAssignedDevs()) {
            User user = getUser(devUsername);
            if (user instanceof Developer) {
                Developer dev = (Developer) user;
                milestone.addObserver(dev);
            }
        }
        milestone.notifyCreated();
    }

    public Milestone getMilestoneFromName(String name) {
        for (Milestone milestone : milestones) {
            if (milestone.getName().equals(name)) {
                return milestone;
            }
        }
        return null;
    }

    public List<Ticket> getTicketsConcerningUser(String username) {
        List<Ticket> filteredTickets = new ArrayList<>();
        User user = getUser(username);

        for (Ticket ticket : tickets) {
            boolean shouldAddTicket = false;

            switch (user.getRole().name()) {
                case "DEVELOPER":
                    if (ticket.getTicketHistory() != null) {
                        for (Ticket.Action action : ticket.getTicketHistory().getActions()) {
                            if (username.equals(action.getBy())) {
                                shouldAddTicket = true;
                                break;
                            }
                        }
                    }

                    if (!shouldAddTicket && ticket.getComments() != null) {
                        for (Ticket.Comment comment : ticket.getComments()) {
                            if (username.equals(comment.getAuthor())) {
                                shouldAddTicket = true;
                                break;
                            }
                        }
                    }
                    break;

                case "MANAGER":
                    List<Milestone> managerMilestones = getMilestonesFromUser(username);

                    for (Milestone milestone : managerMilestones) {
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
            int dateCompare = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (dateCompare != 0)
                return dateCompare;
            return Integer.compare(t1.getId(), t2.getId());
        });

        return filteredTickets;
    }

    public List<Milestone> getMilestonesFromUser(String user) {
        List<Milestone> userMilestones = new ArrayList<>();

        for (Milestone milestone : milestones) {
            if (milestone.getOwner().equals(user)) {
                userMilestones.add(milestone);
            }
        }

        return userMilestones;
    }

    private List<Milestone> getMilestonesForDeveloper(String username) {
        List<Milestone> devMilestones = new ArrayList<>();
        for (Milestone milestone : milestones) {
            if (Arrays.asList(milestone.getAssignedDevs()).contains(username)) {
                devMilestones.add(milestone);
            }
        }
        return devMilestones;
    }

    public void blockMilestone(String name) {
        milestones.stream()
                .filter(milestone -> name.equals(milestone.getName()))
                .findFirst()
                .ifPresent(milestone -> milestone.setBlocked(true));
    }

    public List<Milestone> getMilestones(String username) {
        int lUnderscore = username.lastIndexOf('_');
        String role = username.substring(lUnderscore + 1);
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

    public void addTicket(CommandInput command) {
        if (!command.params().type().equals("BUG") && command.params().reportedBy().isEmpty()) {
            IOUtil.ticketError(command, "ANON");
            return;
        }
        if (!command.params().reportedBy().isEmpty() && !userExists(command.username())) {
            IOUtil.ticketError(command, "NUSR");
            return;
        }
        tickets.add(
                switch (command.params().type()/* .toUpperCase() */ ) {
                    case "BUG" -> {
                        Bug bug = new Bug.Builder()
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
                                // not verified that it works if somethig break around test this
                                .errorCode(command.params().errorCode() != null
                                        ? Integer.parseInt(command.params().errorCode())
                                        : 0)
                                .createdAt(command.timestamp())
                                .build();
                        yield bug;
                    }
                    case "FEATURE_REQUEST" -> {
                        FeatureRequest fr = new FeatureRequest.Builder()
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
                        UIFeedback ui = new UIFeedback.Builder()
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

    public boolean userExists(String username) {
        return users.stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    public String getUsersDb() {
        return USERS_DB;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<UserInput> inputs) {
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

    public List<Ticket> getTickets(String username) {
        int lUnderscore = username.lastIndexOf('_');
        String role = username.substring(lUnderscore + 1);
        switch (role) {
            case "manager":
                return new ArrayList<>(tickets);
            case "reporter":
                return tickets.stream()
                        .filter(ticket -> username.equals(ticket.getReportedBy()))
                        .collect(Collectors.toList());
            default:
                // TODO: change this later i dont like it
                List<Milestone> devMilestones = milestones.stream()
                        .filter(milestone -> milestone.getAssignedDevs() != null &&
                                Arrays.stream(milestone.getAssignedDevs())
                                        .anyMatch(dev -> dev.equals(username)))
                        .collect(Collectors.toList());

                List<Ticket> result = new ArrayList<>();

                for (Milestone milestone : devMilestones) {
                    for (int ticketId : milestone.getTickets()) {
                        for (Ticket ticket : tickets) {
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

    public void addComment(CommandInput command) {
        // System.out.println("entered add comment");
        CommentValidationHandler validateComment = new TicketExistenceHandler();
        validateComment.setNext(new AnonymousTicketHandler())
                .setNext(new ClosedTicketHandler())
                .setNext(new CommentLengthHandler())
                .setNext(new DeveloperAssignmentHandler())
                .setNext(new ReporterOwnershipHandler());

        Ticket ticket = getTicket(command.ticketID());

        boolean isValid = validateComment.validate(command);
        if (!isValid) {
            // System.out.println("not valid");

            return;
        }
        ticket.addComment(command.username(), command.comment(), command.timestamp());
        // System.out.println("exited peacefully");

    }

    public void undoAddComment(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());
        // verifica si daca commentul exista !

        if (ticket == null) {
            // System.out.println("nullcomm");
            return;
        }
        if (ticket.getReportedBy().isEmpty()) {
            IOUtil.commentError(command, "ANON");
            return;
        }
        // if (ticket.isWasError()) {
        // IOUtil.commentError(command, "UNDO");
        // }
        ticket.undoAddComment(command.username());
    }

    public void changeStatus(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());
        Developer dev = (Developer) getUser(command.username());
        if (!ticket.getAssignedTo().equals(command.username())) {
            IOUtil.changeError(command, "ASSIGNMENT");
            return;
        }

        Status oldStatus = ticket.getStatus();

        switch (oldStatus.name()) {
            case "IN_PROGRESS":
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(), command.timestamp());
                dev.setClosedTickets(dev.getClosedTickets() + 1);
                break;
            case "RESOLVED":
                ticket.changeStatus(Ticket.Status.CLOSED, command.username(), command.timestamp());
                Milestone milestone = getMilestoneFromTicketID(command.ticketID());
                milestone.changeStatusOfTicket(command.ticketID());
                // dev.setClosedTickets(dev.getClosedTickets() + 1);
                break;
            default:
                break;
        }
    }

    public void undoChangeStatus(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());

        if (!ticket.getAssignedTo().equals(command.username())) {
            IOUtil.changeError(command, "ASSIGNMENT");
            return;
        }

        Status currentStatus = ticket.getStatus();

        switch (currentStatus.name()) {
            case "RESOLVED":
                ticket.changeStatus(Ticket.Status.IN_PROGRESS, command.username(), command.timestamp());
                break;
            case "CLOSED":
                ticket.changeStatus(Ticket.Status.RESOLVED, command.username(), command.timestamp());
                Milestone milestone = getMilestoneFromTicketID(command.ticketID());
                milestone.undoChangeStatusOfTicket(command.ticketID());
                break;
            default:
                break;
        }
    }

    public TicketHistory getTicketHistory(int id) {
        Ticket ticket = getTicket(id);
        return ticket.getTicketHistory();
    }

    public void update(LocalDate date) {
        milestones.forEach(milestone -> {
            int timeLeft = (int) ChronoUnit.DAYS.between(date, LocalDate.parse(milestone.getDueDate()));

            if (milestone.getCompletionPercentage() == 1.0) {
                milestone.setStatus("COMPLETED");
            }
            if (milestone.getStatus().equals("COMPLETED")) {
                return;
            }
            // this should be in the milestone class maybe ? not here idk
            // like how i do for upPriority() edit: maybe to contorted since in the
            // milestone class you have to call here to do the actual changes since here lie
            // the actual tickets
            if (timeLeft < 0) {
                milestone.setDaysUntilDue(0);
                milestone.setOverdueBy(-timeLeft + 1);
            } else {
                milestone.setDaysUntilDue(timeLeft + 1);
                milestone.setOverdueBy(0);
            }

            int timeSinceCreation = Math
                    .abs((int) ChronoUnit.DAYS.between(LocalDate.parse(milestone.getCreatedAt()), date));
            // monitor this method since im not sure what consitutes 3 days
            if ((timeSinceCreation != 0) && !milestone.isBlocked()) {
                boolean CRIT = false;
                if (timeLeft <= 1) {
                    CRIT = true;
                    if (timeLeft == 1) {
                        milestone.notifyDueTomorrow();
                    }
                }
                for (int ticketId : milestone.getTickets()) {
                    for (Ticket ticket : tickets) {
                        if (ticketId == ticket.getId()) {
                            if (ticket.getSolvedAt() != null) {
                                continue;
                            }
                            if (CRIT) {
                                // wrap this in a method since i dont think database should be aware of
                                // businessPriority
                                ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                                // System.out.println("CRITICAL " + ticket.getId());
                            }
                            if (timeSinceCreation % 3 == 0) {
                                ticket.upPriority();
                            }
                        }
                    }
                }
            }
        });
    }

    // TODO CHORE: DUPLICATE CODE FOR THESE FUCKASS STATS
    public List<Number> getCustomerImpact() {
        List<Number> report = new ArrayList<>();

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

        for (Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            String type = ticket.getType();
            String priority = ticket.getBusinessPriority().name();
            double impact = ticket.getImpact();

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
        List<Number> report = new ArrayList<>();

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

        for (Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("OPEN")) {
                continue;
            }
            if (ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }
            totalTickets++;
            String type = ticket.getType();
            String priority = ticket.getBusinessPriority().name();
            double impact = ticket.getEfficiency();

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
        List<Object> report = new ArrayList<>();

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

        for (Ticket ticket : tickets) {
            if (!ticket.getStatus().name().equals("OPEN") &&
                    !ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }

            totalOpenTickets++;
            String type = ticket.getType();
            String priority = ticket.getBusinessPriority().name();

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

        double avgBugRisk = bugCount > 0 ? bugRiskScore / bugCount : 0;
        double avgFeatureRisk = featureRequestCount > 0 ? featureRequestRiskScore / featureRequestCount : 0;
        double avgUIRisk = uiFeedbackCount > 0 ? uiFeedbackRiskScore / uiFeedbackCount : 0;

        String bugRiskLevel = getRiskLevel(avgBugRisk);
        String featureRiskLevel = getRiskLevel(avgFeatureRisk);
        String uiRiskLevel = getRiskLevel(avgUIRisk);

        report.add(bugRiskLevel);
        report.add(featureRiskLevel);
        report.add(uiRiskLevel);

        double avgBugImpact = bugCount > 0 ? bugImpact / bugCount : 0;
        double avgFeatureImpact = featureRequestCount > 0 ? featureRequestImpact / featureRequestCount : 0;
        double avgUIImpact = uiFeedbackCount > 0 ? uiFeedbackImpact / uiFeedbackCount : 0;

        report.add(MathUtil.round(avgBugImpact));
        report.add(MathUtil.round(avgFeatureImpact));
        report.add(MathUtil.round(avgUIImpact));

        String appStability = determineStability(bugRiskLevel, featureRiskLevel, uiRiskLevel,
                avgBugImpact, avgFeatureImpact, avgUIImpact);
        report.add(appStability);

        return report;
    }

    public List<List<Object>> getPerformance(CommandInput command) {
        List<List<Object>> report = new ArrayList<>();
        Manager manager = (Manager) getUser(command.username());

        List<String> subordinateUsernames = Arrays.asList(manager.getSubordinates());
        List<String> sortedUsernames = new ArrayList<>(subordinateUsernames);
        Collections.sort(sortedUsernames);

        for (String username : sortedUsernames) {
            Developer dev = (Developer) getUser(username);
            if (dev != null) {
                List<Object> row = new ArrayList<>();

                row.add(username);
                List<Number> metrics = dev.updatePerformanceScore(command.time());
                row.add(metrics.get(0).intValue());
                row.add(MathUtil.round(metrics.get(2).doubleValue()));
                row.add(MathUtil.round(metrics.get(1).doubleValue()));
                row.add(dev.getSeniority().toString());

                report.add(row);
            }
        }

        return report;
    }

    private String determineStability(String bugRisk, String featureRisk, String uiRisk,
            double bugImpact, double featureImpact, double uiImpact) {

        boolean hasSignificantRisk = bugRisk.equals("SIGNIFICANT") ||
                bugRisk.equals("MAJOR") ||
                featureRisk.equals("SIGNIFICANT") ||
                featureRisk.equals("MAJOR") ||
                uiRisk.equals("SIGNIFICANT") ||
                uiRisk.equals("MAJOR");

        boolean allNegligible = bugRisk.equals("NEGLIGIBLE") &&
                featureRisk.equals("NEGLIGIBLE") &&
                uiRisk.equals("NEGLIGIBLE");

        boolean allImpactBelow50 = bugImpact < 50 && featureImpact < 50 && uiImpact < 50;

        if (hasSignificantRisk) {
            return "UNSTABLE";
        }

        if (allNegligible && allImpactBelow50) {
            return "STABLE";
        }

        return "PARTIALLY STABLE";
    }

    public List<Object> getTicketRisk() {
        List<Object> report = new ArrayList<>();

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

        for (Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            String type = ticket.getType();
            String priority = ticket.getBusinessPriority().name();
            double impact = ticket.getRisk();

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

        String bugRiskLevel = getRiskLevel(bugImpact);
        String featureRequestRiskLevel = getRiskLevel(featureRequestImpact);
        String uiFeedbackRiskLevel = getRiskLevel(uiFeedbackImpact);

        report.add(bugRiskLevel);
        report.add(featureRequestRiskLevel);
        report.add(uiFeedbackRiskLevel);

        return report;
    }

    private String getRiskLevel(double impact) {
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
        List<Developer> developers = new ArrayList<Developer>();
        for (User user : users) {
            if (user.getRole().name().equals("DEVELOPER")) {
                developers.add((Developer) user);
            }
        }
        return developers;
    }

    public List<?> getSearchResults(CommandInput command) {
        User user = getUser(command.username());
        FiltersInput filters = command.filters();
        String searchType = filters.searchType();

        if ("DEVELOPER".equals(searchType)) {
            if (!"MANAGER".equals(user.getRole().name())) {
                return new ArrayList<>();
            }

            List<Developer> allDevelopers = getAllDevelopers();
            return filterDevelopers((Manager) user, allDevelopers, filters);

        } else {
            List<Ticket> allTickets = getAllTickets();
            return filterTickets(user, allTickets, filters);
        }
    }

    private List<Ticket> filterTickets(User user, List<Ticket> allTickets, FiltersInput filters) {
        List<Ticket> accessibleTickets = new ArrayList<>();

        if ("MANAGER".equals(user.getRole().name())) {
            accessibleTickets.addAll(allTickets);
        } else if ("DEVELOPER".equals(user.getRole().name())) {
            Developer dev = (Developer) user;
            List<Milestone> devMilestones = getMilestonesForDeveloper(dev.getUsername());

            for (Milestone milestone : devMilestones) {
                for (int ticketId : milestone.getTickets()) {
                    for (Ticket ticket : allTickets) {
                        if (ticket.getId() == ticketId &&
                                ticket.getStatus() == Ticket.Status.OPEN) {

                            boolean alreadyAdded = false;
                            for (Ticket addedTicket : accessibleTickets) {
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

        FilterContext<Ticket> context = new FilterContext<>();

        context.addStrategy("businessPriority", new BusinessPriorityFilter());
        context.addStrategy("type", new TypeFilter());
        context.addStrategy("createdAt", new CreatedAtFilter());
        context.addStrategy("createdBefore", new CreatedBeforeFilter());
        context.addStrategy("createdAfter", new CreatedAfterFilter());
        context.addStrategy("keywords", new KeywordsFilter());

        if ("DEVELOPER".equals(user.getRole().name())) {
            Developer dev = (Developer) user;
            context.addStrategy("availableForAssignment", new AvailableForAssignmentFilter(dev));
        }

        Map<String, String> filterMap = convertToMap(filters);
        List<Ticket> filtered = context.applyFilters(accessibleTickets, filterMap);

        filtered.sort(Comparator
                .comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        return filtered;
    }

    private List<Developer> filterDevelopers(Manager manager, List<Developer> allDevelopers,
            FiltersInput filters) {
        List<Developer> subordinates = new ArrayList<>();
        List<String> subordinateUsernames = Arrays.asList(manager.getSubordinates());

        for (Developer dev : allDevelopers) {
            if (subordinateUsernames.contains(dev.getUsername())) {
                subordinates.add(dev);
            }
        }

        FilterContext<Developer> context = new FilterContext<>();

        context.addStrategy("expertiseArea", new ExpertiseAreaFilter());
        context.addStrategy("seniority", new SeniorityFilter());
        context.addStrategy("performanceScoreAbove", new PerformanceScoreAboveFilter());
        context.addStrategy("performanceScoreBelow", new PerformanceScoreBelowFilter());

        Map<String, String> filterMap = convertToMap(filters);
        List<Developer> filtered = context.applyFilters(subordinates, filterMap);

        filtered.sort(Comparator.comparing(Developer::getUsername));

        return filtered;
    }

    private Map<String, String> convertToMap(FiltersInput filters) {
        Map<String, String> map = new HashMap<>();

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

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<CommandInput> getCommands() {
        return commands;
    }

    public void setCommands(List<CommandInput> commands) {
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
