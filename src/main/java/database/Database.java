package database;

import tickets.Bug;
import tickets.FeatureRequest;
import tickets.Ticket;
import tickets.UIFeedback;
import io.CommandInput;
import io.IOUtil;
import io.UserInput;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import users.User;
import users.Manager;
import users.Developer;
import users.Reporter;

public class Database {
    private static final String USERS_DB = "input/database/users.json";

    private static List<User> users = new ArrayList<>(); // daken from the db file
    private static List<Ticket> tickets = new ArrayList<>(); // input in testing period
    private static List<CommandInput> commands = new ArrayList<>();

    public static void addTicket(CommandInput command, LocalDate currentDate) {
        // im too lazy right now but before you do this func check how the inputs should
        // look like, maybe you should link the tickets to an actual person if its
        // mandatory but afaik for bugs its not so you can either create a anon user and
        // add all anoms to him or maybe not link at all i cba this is too boring

        if (!command.params().type().equals("BUG") && command.params().reportedBy().isEmpty()) {
            // if you need a chain of responsability you can do one here where you pass the
            // command to a handleCommandError(command) and it figures out the error to
            // print based on dif things i guess
            IOUtil.ticketError(command, "ANON");
        }
        // either add a null user or check in here for them think abt it 
        // if (!users.exists(command.username()))
        tickets.add(
                switch (command.params().type().toUpperCase()) {
                    case "BUG" -> {
                        Bug bug = new Bug.Builder()
                                .id(Ticket.getTicketId())
                                .title(command.params().title())
                                .businessPriority(Ticket.BusinessPriority.valueOf(
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
                                .createdAt(currentDate.toString())
                                .build();
                        yield bug;
                    }
                    case "FEATURE_REQUEST" -> {
                        FeatureRequest fr = new FeatureRequest.Builder()
                                .id(Ticket.getTicketId())
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
                                .createdAt(currentDate.toString())
                                .build();
                        yield fr;
                    }
                    case "UI_FEEDBACK" -> {
                        UIFeedback ui = new UIFeedback.Builder()
                                .id(Ticket.getTicketId())
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
                                .createdAt(currentDate.toString())
                                .build();
                        yield ui;
                    }
                    default -> throw new IllegalArgumentException(
                            "Unknown ticket type: " + command.params().type());
                });
        // IF PROBLEMS IWTH ID FIX THIS
        Ticket.setTicketId(Ticket.getTicketId() + 1);
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
                System.out.println("implement the rest (devs i think)");
        }
        return tickets;
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
