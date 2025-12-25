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
    private static LocalDate second;
    private static List<CommandInput> commands;

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath  path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */

    public static void testingPeriod() {
        LocalDate initialDate = second;
        LocalDate finishDate = second.plusDays(12);

        LocalDate time = initialDate;
        int it = 0;

        while (!time.isAfter(finishDate)) {
            time = commands.get(it).time();
            /*
             *give every ticket an id (keep a static variable to keep count of them (first check what rule they follow for example what if another testingperiod starts))
             *verify if the data is fine (only bug tickets can be anonym or sum)
             *create a list to hold the tickets
             *create a method to viewTickets
            */
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
        testingPeriod();
        developPeriod();
        verifiyPeriod();
        // }

        // TODO 3: create objectnodes for output, add them to outputs list.

        IOUtil.writeOutput();
    }
}
