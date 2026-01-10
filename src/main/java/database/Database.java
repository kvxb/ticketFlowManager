package database;

import tickets.Bug;
import java.util.Comparator;
import tickets.FeatureRequest;
import tickets.Ticket;
import tickets.UIFeedback;
import tickets.Ticket.BusinessPriority;
import tickets.Ticket.Status;
import milestones.Milestone;
import io.CommandInput;
import io.IOUtil;
import io.UserInput;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import validation.DeveloperValidationHandler;
import validation.ExpertiseAreaHandler;
import validation.LockedMilestoneHandler;
import validation.MilestoneAssignmentHandler;
import validation.SeniorityLevelHandler;
import validation.TicketStatusHandler;
import milestones.Milestone.Repartition;

import users.User;
import users.Manager;
import users.Developer;
import users.Reporter;

public class Database {
    private static final String USERS_DB = "input/database/users.json";

    private static List<User> users = new ArrayList<>(); // daken from the db file
    private static List<Ticket> tickets = new ArrayList<>(); // input in testing period
    private static List<CommandInput> commands = new ArrayList<>();
    private static List<Milestone> milestones = new ArrayList<>();

    public static int getSize(String who) {
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

    public static void clearDatabase() {
        users.clear();
        tickets.clear();
        commands.clear();
        milestones.clear();
        // System.out.println("cleared all");
    }

    public static String getMilestoneNameFromTicketID(int TicketID) {
        for (Milestone m : milestones) {
            for (int id : m.getTickets()) {
                if (id == TicketID) {
                    return m.getName();
                }
            }
        }
        return null;
    }

    public static Milestone getMilestoneFromTicketID(int TicketID) {
        for (Milestone m : milestones) {
            for (int id : m.getTickets()) {
                if (id == TicketID) {
                    return m;
                }
            }
        }
        return null;
    }

    // TODO: CHORE: DOESNT command hold the date :): (no need for both parameters
    // rewrite all methods like this one)
    public static void assignTicket(CommandInput command) {
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

    public static User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static Ticket getTicket(int id) {
        for (Ticket t : tickets) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public static List<Ticket> getAssignedTickets(String username) {
        return tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() != null &&
                        ticket.getAssignedTo().equals(username))
                .sorted(Comparator
                        .comparing(Ticket::getBusinessPriority).reversed()
                        .thenComparing(Ticket::getId))
                .collect(Collectors.toList());
    }

    public static void addMilestone(CommandInput command) {
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
    }

    public static void blockMilestone(String name) {
        milestones.stream()
                .filter(milestone -> name.equals(milestone.getName()))
                .findFirst()
                .ifPresent(milestone -> milestone.setBlocked(true));
    }

    public static List<Milestone> getMilestones(String username) {
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

    public static void addTicket(CommandInput command) {
        // im too lazy right now but before you do this func check how the inputs should
        // look like, maybe you should link the tickets to an actual person if its
        // mandatory but afaik for bugs its not so you can either create a anon user and
        // add all anoms to him or maybe not link at all i cba this is too boring

        if (!command.params().type().equals("BUG") && command.params().reportedBy().isEmpty()) {
            // if you need a chain of responsability you can do one here where you pass the
            // command to a handleCommandError(command) and it figures out the error to
            // print based on dif things i guess
            IOUtil.ticketError(command, "ANON");
            return;
        }
        // either add a null user or check in here for them think abt it
        if (!command.params().reportedBy().isEmpty() && !userExists(command.username())) {
            IOUtil.ticketError(command, "NUSR");
            return;
        }
        // TODO tidy this up
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
        // IF PROBLEMS IWTH ID FIX THIS
        Ticket.setTicketId(Ticket.getTicketId() + 1);
    }

    public static boolean userExists(String username) {
        return users.stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    public static String getUsersDb() {
        return USERS_DB;
    }

    public static List<User> getUsers() {
        return users;
    }

    public static void setUsers(List<UserInput> inputs) {
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

    public static List<Ticket> getTickets(String username) {
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

    public static void undoAssignedTicket(CommandInput command) {
        Ticket tkt = tickets.stream()
                .filter(ticket -> ticket.getId() == command.ticketID())
                .findFirst()
                .orElse(null);

        if (tkt == null) {
            System.out.println("didnt find ticket");
            return;
            // TODO: add error here
        }
        tkt.setAssignedAt(null);
        tkt.setAssignedTo(null);
        tkt.setStatus(Ticket.Status.OPEN);
        System.out.println("finished with ticket" + tkt.getId());
    }

    public static void addComment(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());

        if (ticket == null) {
            // System.out.println("nullcomm");
            return;
        }

        // System.out.println("comm");

        ticket.addComment(command.username(), command.comment(), command.timestamp());
    }

    public static void undoAddComment(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());
        //verifica si daca commentul exista !

        if (ticket == null) {
            // System.out.println("nullcomm");
            return;
        }

        System.out.println("undosuccess");

        ticket.undoAddComment(command.username());
    }

    public static void update(LocalDate date) {
        milestones.forEach(milestone -> {
            int timeLeft = (int) ChronoUnit.DAYS.between(date, LocalDate.parse(milestone.getDueDate()));
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
                }
                for (int ticketId : milestone.getTickets()) {
                    for (Ticket ticket : tickets) {
                        if (ticketId == ticket.getId()) {
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

    public static List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }

    public static void setTickets(List<Ticket> tickets) {
        Database.tickets = tickets;
    }

    public static List<CommandInput> getCommands() {
        return commands;
    }

    public static void setCommands(List<CommandInput> commands) {
        Database.commands = commands;
    }

}
