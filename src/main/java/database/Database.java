package database;

import tickets.Bug;
import tickets.FeatureRequest;
import tickets.Ticket;
import io.CommandInput;
import users.User;

import java.util.List;

import javax.xml.crypto.Data;

public class Database {
    private static final String USERS_DB = "input/database/users.json";

    private static List<User> users; // daken from the db file
    private static List<Ticket> tickets; // input in testing period
    private static List<CommandInput> commands;

    public static void addTicket(CommandInput command) {
        // im too lazy right now but before you do this func check how the inputs should
        // look like, maybe you should link the tickets to an actual person if its
        // mandatory but afaik for bugs its not so you can either create a anon user and
        // add all anoms to him or maybe not link at all i cba this is too boring
        tickets.add(
                switch (command.params().type()) {
                    case "BUG" -> new Bug();
                    case "FEATURE_REQUEST" -> new FeatureRequest();
                    case "UI_FEEDBACK" -> new Ticket();
                    default -> throw new IllegalArgumentException("Unknown type");
                });
    }

    public static String getUsersDb() {
        return USERS_DB;
    }

    public static List<User> getUsers() {
        return users;
    }

    public static void setUsers(List<User> users) {
        Database.users = users;
    }

    public static List<Ticket> getTickets() {
        return tickets;
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
