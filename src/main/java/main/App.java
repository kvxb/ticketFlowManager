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

    private static LocalDate currentDate;

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
        // read commands in the 12 days period after the start of this method
        // initialize tickets validate them add them to db
        LocalDate endDate = currentDate.plusDays(12);
        int it = 0;

        while(!currentDate.isAfter(endDate)) {
            CommandInput currentCommand = Database.getCommands().get(it);
            currentDate = currentCommand.time();
            switch(currentCommand.command()) {
                case "reportTicket":
                    Database.addTicket(currentCommand);
                case "viewTickets":
                    IOUtil.viewTickets(currentCommand, Database.getTickets(currentCommand.username()));
            }
        }
    }

    public static void developPeriod() {
        LocalDate initialDate = currentDate;

    }

    public static void verifiyPeriod() {
        LocalDate initialDate = currentDate;

    }

    public static void run(final String inputPath, final String outputPath) {
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
            verifiyPeriod();
            LOOPBACK = false;
        }
        // TODO 3: create objectnodes for output, add them to outputs list.

        IOUtil.writeOutput();
    }
}
