package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import database.Database;
import users.User;
import tickets.Ticket;

import java.util.List;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

public class IOUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static List<ObjectNode> outputs = new ArrayList<>();
    private static String inputPath;
    private static String outputPath;

    private IOUtil() {

    }

    public static List<CommandInput> readCommands() throws IOException {
        File inputFile = new File(inputPath);

        return MAPPER.readerForListOf(CommandInput.class)
                .readValue(inputFile);
    }

    public static List<UserInput> readUsers() throws IOException {
        File inputFile = new File(Database.getUsersDb());

        return MAPPER.readerForListOf(UserInput.class)
                .readValue(inputFile);
    }

    public static void viewTickets(CommandInput command, List<Ticket> tickets) {
        ObjectNode commandNode = MAPPER.createObjectNode();

        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ArrayNode ticketsArray = MAPPER.createArrayNode();

        for (Ticket ticket : tickets) {
            ObjectNode ticketNode = MAPPER.createObjectNode();

            ticketNode.put("id", ticket.getId());
            ticketNode.put("type", ticket.getType());
            ticketNode.put("title", ticket.getTitle());
            ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
            ticketNode.put("status", ticket.getStatus().toString());

            ticketNode.put("createdAt", ticket.getCreatedAt() != null ? ticket.getCreatedAt() : "");
            ticketNode.put("assignedAt", ticket.getAssignedAt() != null ? ticket.getAssignedAt() : "");
            ticketNode.put("solvedAt", ticket.getSolvedAt() != null ? ticket.getSolvedAt() : "");
            ticketNode.put("assignedTo", ticket.getAssignedTo() != null ? ticket.getAssignedTo() : "");
            ticketNode.put("reportedBy", ticket.getReportedBy() != null ? ticket.getReportedBy() : "");

            ArrayNode commentsArray = MAPPER.createArrayNode();
            ticketsArray.add(ticketNode);
        }

        commandNode.set("tickets", ticketsArray);
        outputs.add(commandNode);
    }

    // public static void writeAll() throws IOException {
    //
    // List<CommandInput> commands = readCommands();
    // for (CommandInput command : commands) {
    // ObjectNode commandNode = MAPPER.convertValue(command, ObjectNode.class);
    // outputs.add(commandNode);
    // }
    // writeOutput();
    // }

    // TODO every interaction with the output should be done sing the Util
    // like IOUtil.addOutput(or sum shit);

    public static void writeOutput() {
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }

    public static void setInputPath(String path) {
        inputPath = path;
    }

    public static void setOutputPath(String path) {
        outputPath = path;
    }

    public static void setPaths(String input, String output) {
        setInputPath(input);
        setOutputPath(output);
    }
}
