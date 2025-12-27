package database;

import tickets.Ticket;
import io.CommandInput;
import users.User;

import java.util.List;

public class Database {
    private List<User> users; //daken from the db file
    private List<Ticket> tickets; // input in testing period
    private List<CommandInput> commands;
}
