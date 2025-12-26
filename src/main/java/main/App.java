package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.IOUtil;
import io.CommandInput;
import io.ParamsInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import tickets.Ticket;

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

    private static final String INPUT_USERS_FIELD = "input/database/users.json";
    private static List<Ticket> tickets;
    private static LocalDate second;
    private static List<CommandInput> commands;

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
        LocalDate initialDate = second;
        LocalDate finishDate = second.plusDays(12);

        LocalDate time = initialDate;
        int it = 0;

        while (!time.isAfter(finishDate)) {
            /*
             * give every ticket an id (keep a static variable to keep count of them (first
             * check what rule they follow for example what if another testingperiod
             * starts))
             * verify if the data is fine (only bug tickets can be anonym or sum)
             * create a list to hold the tickets
             * create a method to viewTickets
             */
            CommandInput currentCommand = commands.get(it);
            time = currentCommand.time();

            it += 1;
        }
    }

    public static void developPeriod() {
        LocalDate initialDate = second;

    }

    public static void verifiyPeriod() {
        LocalDate initialDate = second;

    }

    public static void run(final String inputPath, final String outputPath) {
        IOUtil.setPaths(inputPath, outputPath);

        try {
            commands = IOUtil.readCommands();
        } catch (IOException e) {
            System.out.println("error reading from input file: " + e.getMessage());
            return;
        }

        second = commands.getFirst().time();

        // TODO 2: process commands.
        /*
         * later on ill have to separate into the stages of development but for now im
         * not sure what exactly are the rules so im just going to
         * work with a simple for loop iterating through days and see what is up later
         * on
         */
        // for(var second =
        // commands.getFirst().time();!second.isAfter(commands.getLast().time()); second
        // = second.plusDays(1)) {
        //
        //
        //
        //
        // either do a while loop through these 3 stages until the thing that stops them
        // happens
        //
        // or make it so that these are the minimal stages and verify can call
        // testingPeriod in the code of it which makes sense code wise as i understand
        // it but might not be optimal for readability reasons
        boolean LOOPBACK = true;
        while (LOOPBACK) {
            testingPeriod();
            developPeriod();
            verifiyPeriod();
        }
        // }

        // TODO 3: create objectnodes for output, add them to outputs list.

        IOUtil.writeOutput();
    }
}
