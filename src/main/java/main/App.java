package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import database.Database;
import io.IOUtil;
import io.CommandInput;
import io.ParamsInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import java.time.LocalDate;
import tickets.Ticket;

import database.Database;

/*
https://www.baeldung.com/jackson-annotations
 */

/**
 * main.App represents the main application logic that processes input commands,
 * generates outputs, and writes them to a file
 */
public class App {
    private App() {
    }

    // do these rlly need to be satatic?
    private static LocalDate currentDate;
    private static int it = 0;

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath  path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */

    // think how this method should work it should be as decoupled from the logic of
    // the commands as that should be implmeneted in their respective classes
    // but then who holds the commands for example ? this class should right ?
    // unless you can make them float as a sort of static class ? think about it
    // tomorrow i cba today
    public static void testingPeriod() {
        System.out.println("testingPeriod");
        // read commands in the 12 days period after the start of this method
        // initialize tickets validate them add them to db
        LocalDate endDate = currentDate.plusDays(12);
        System.out.println("testing" + currentDate);

        // LocalDate futureDate = currentDate;

        while (it < Database.getSize("commands")) {
            CommandInput currentCommand = Database.getCommands().get(it);
            currentDate = currentCommand.time();
            // futureDate = Database.getCommands().get(it + 1).time();
            if (currentDate.isAfter(endDate)) {
                currentDate = endDate;
                break;
            }
            Database.update(currentCommand.time());
            switch (currentCommand.command()) {
                case "reportTicket":
                    Database.addTicket(currentCommand);
                    break;
                case "viewTickets":
                    IOUtil.viewTickets(currentCommand, Database.getTickets(currentCommand.username()));
                    break;
                default:
                    System.out.println("didnt match command");
            }
            it++;
        }
    }

    public static void developPeriod() {
        System.out.println("developPeriod");
        LocalDate endDate = currentDate.plusDays(100);

        while (it < Database.getSize("commands")) {
            CommandInput currentCommand = Database.getCommands().get(it);
            currentDate = currentCommand.time();
            // futureDate = Database.getCommands().get(it + 1).time();
            if (currentDate.isAfter(endDate)) {
                break;
            }
            Database.update(currentCommand.time());
            System.out.println("develop" + currentDate + currentCommand.command());

            // swtich to ->
            switch (currentCommand.command()) {
                case "reportTicket":
                    IOUtil.ticketError(currentCommand, "WPER");
                    break;
                case "viewTickets":
                    IOUtil.viewTickets(currentCommand, Database.getTickets(currentCommand.username()));
                    break;
                case "createMilestone":
                    Database.addMilestone(currentCommand);
                    break;
                case "viewMilestones":
                    IOUtil.viewMilestones(currentCommand, Database.getMilestones(currentCommand.username()));
                    break;
                case "assignTicket":
                    Database.assignTicket(currentCommand);
                    break;
                case "viewAssignedTickets":
                    IOUtil.viewAssignedTickets(currentCommand, Database.getAssignedTickets(currentCommand.username()));
                    break;
                case "undoAssignTicket":
                    Database.undoAssignedTicket(currentCommand);
                    break;
                case "addComment":
                    Database.addComment(currentCommand);
                    break;
                case "undoAddComment":
                    Database.undoAddComment(currentCommand);
                    break;
                case "changeStatus":
                    Database.changeStatus(currentCommand);
                    break;
                case "viewTicketHistory":
                    IOUtil.viewTicketHistory(currentCommand, Database.getTicketsConcerningUser(currentCommand.username()));
                    break;
                default:
                    System.out.println("didnt match command");
            }
            it++;

        }

    }

    // public static void verifiyPeriod() {
    // System.out.println("verifiyPeriod");
    // LocalDate endDate = currentDate.plusDays(12);
    //
    // while (it <= Database.getSize("commands")) {
    // CommandInput currentCommand = Database.getCommands().get(it);
    // currentDate = currentCommand.time();
    // // futureDate = Database.getCommands().get(it + 1).time();
    // if (currentDate.isAfter(endDate)) {
    // it--;
    // break;
    // }
    // switch (currentCommand.command()) {
    // case "reportTicket":
    // Database.addTicket(currentCommand, currentDate);
    // break;
    // case "viewTickets":
    // IOUtil.viewTickets(currentCommand,
    // Database.getTickets(currentCommand.username()));
    // break;
    // default:
    // System.out.println("vv");
    // }
    // it++;
    // }
    // System.out.println("verifiy" + currentDate);
    //
    // }

    public static void run(final String inputPath, final String outputPath) {
        Database.clearDatabase();
        IOUtil.clearIO();
        Ticket.clearTicket();
        it = 0;

        IOUtil.setPaths(inputPath, outputPath);
        try {
            Database.setUsers(IOUtil.readUsers());
            Database.setCommands(IOUtil.readCommands());
        } catch (IOException e) {
            System.out.println("error reading from input file: " + e.getMessage());
            return;
        }

        currentDate = Database.getCommands().getFirst().time();

        // TODO 2: process commands.
        boolean LOOPBACK = true;
        while (LOOPBACK) {
            testingPeriod();
            developPeriod();
            // verifiyPeriod();
            LOOPBACK = false;
        }
        // TODO 3: create objectnodes for output, add them to outputs list.

        IOUtil.writeOutput();
        System.out.println("END" + outputPath);
    }
}
