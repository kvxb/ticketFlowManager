package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.lang.Object;
import database.Database;
import milestones.Milestone;
import milestones.Milestone.Repartition;
import users.Developer;
import users.User;
import tickets.Ticket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class IOUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static List<ObjectNode> outputs = new ArrayList<>();
    private static String inputPath;
    private static String outputPath;

    private IOUtil() {

    }

    public static void clearIO() {
        outputs.clear();
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

    public static void viewAssignedTickets(CommandInput command, List<Ticket> tickets) {
        ObjectNode commandNode = MAPPER.createObjectNode();

        // TODO: these can be modularized since every output i think has them check it
        // out
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
            ticketNode.put("reportedBy", ticket.getReportedBy() != null ? ticket.getReportedBy() : "");

            ArrayNode commentsArray = MAPPER.createArrayNode();
            ticket.getComments().stream()
                    .forEach(comment -> {
                        ObjectNode commentNode = MAPPER.createObjectNode();
                        commentNode.put("author", comment.getAuthor());
                        commentNode.put("content", comment.getContent());
                        commentNode.put("createdAt", comment.getCreatedAt());
                        commentsArray.add(commentNode);
                    });
            ticketNode.set("comments", commentsArray);
            ticketsArray.add(ticketNode);
        }

        commandNode.set("assignedTickets", ticketsArray);
        outputs.add(commandNode);

    }

    // is this move or copy semantics ?
    public static void viewTickets(CommandInput command, List<Ticket> tickets) {
        ObjectNode commandNode = MAPPER.createObjectNode();

        // TODO: these can be modularized since every output i think has them check it
        // out
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
            ticket.getComments().stream()
                    .forEach(comment -> {
                        ObjectNode commentNode = MAPPER.createObjectNode();
                        commentNode.put("author", comment.getAuthor());
                        commentNode.put("content", comment.getContent());
                        commentNode.put("createdAt", comment.getCreatedAt());
                        commentsArray.add(commentNode);
                    });
            ticketNode.set("comments", commentsArray);
            ticketsArray.add(ticketNode);
        }

        commandNode.set("tickets", ticketsArray);
        outputs.add(commandNode);
    }

    public static void viewMilestones(CommandInput command, List<Milestone> unsortedMilestones) {
        // TODO move these to the Database function and give them to IOUTIL
        // look at assignedmmilestone for help im so lazy this cant be
        List<Milestone> sortedMilestones = unsortedMilestones.stream()
                .sorted(Comparator
                        .comparing(Milestone::getDueDate)
                        .thenComparing(Milestone::getName))
                .collect(Collectors.toList());

        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ArrayNode milestonesArray = MAPPER.createArrayNode();

        // System.out.println("----------------");
        // System.out.println(milestones.size());
        // System.out.println("----------------");

        for (Milestone milestone : sortedMilestones) {
            ObjectNode ticketNode = MAPPER.createObjectNode();

            ticketNode.put("name", milestone.getName());
            ArrayNode blockingFor = MAPPER.createArrayNode();
            Arrays.stream(milestone.getBlockingFor())
                    .forEach(blockingFor::add);
            ticketNode.set("blockingFor", blockingFor);
            ticketNode.put("dueDate", milestone.getDueDate());
            ticketNode.put("createdAt", milestone.getCreatedAt());
            ArrayNode tickets = MAPPER.createArrayNode();
            Arrays.stream(milestone.getTickets())
                    .forEach(tickets::add);
            ticketNode.set("tickets", tickets);
            ArrayNode assignedDevs = MAPPER.createArrayNode();
            Arrays.stream(milestone.getAssignedDevs())
                    .forEach(assignedDevs::add);
            ticketNode.set("assignedDevs", assignedDevs);

            ticketNode.put("createdBy", milestone.getOwner());
            ticketNode.put("status", milestone.getStatus());
            ticketNode.put("isBlocked", milestone.isBlocked());
            ticketNode.put("daysUntilDue", milestone.getDaysUntilDue());

            ticketNode.put("overdueBy", milestone.getOverdueBy());
            ArrayNode openTickets = MAPPER.createArrayNode();
            Arrays.stream(milestone.getOpenTickets())
                    .forEach(openTickets::add);
            ticketNode.set("openTickets", openTickets);
            ArrayNode closedTickets = MAPPER.createArrayNode();
            ticketNode.set("closedTickets", closedTickets);
            ticketNode.put("completionPercentage", milestone.getCompletionPercentage());

            ArrayNode repartition = Arrays.stream(milestone.getRepartitions())
                    .filter(rep -> rep != null)
                    // TODO: make sure these are needed
                    .filter(rep -> rep.getDev() != null)
                    .map(rep -> {
                        ObjectNode devNode = MAPPER.createObjectNode();
                        devNode.put("developer", rep.getDev());

                        ArrayNode assignedArray = MAPPER.createArrayNode();

                        if (rep.getAssignedTickets() != null) {
                            rep.getAssignedTickets().forEach(assignedArray::add);
                        }

                        devNode.set("assignedTickets", assignedArray);
                        return devNode;
                    })
                    .collect(
                            () -> MAPPER.createArrayNode(),
                            ArrayNode::add,
                            ArrayNode::addAll);

            ticketNode.set("repartition", repartition);
            milestonesArray.add(ticketNode);
        }
        commandNode.set("milestones", milestonesArray);
        outputs.add(commandNode);
    }

    // all these errors can be made into one maybe ?
    public static void assignError(CommandInput command, String errorType) {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("command", command.command());
        error.put("username", command.username());
        error.put("timestamp", command.timestamp());

        switch (errorType) {
            case "STATUS" ->
                error.put("error", "Only OPEN tickets can be assigned.");
            case "SENIORITY" ->
                error.put("error", "Developer " + command.username() + " cannot assign ticket " + command.ticketID()
                        + " due to seniority level. Required: "
                        + Database.getTicket(command.ticketID()).getRequiredSeniority() + "; Current: "
                        + ((Developer) Database.getUser(command.username())).getSeniority() + ".");
            case "ASSIGNMENT" ->
                error.put("error", "Developer " + command.username() + " is not assigned to milestone "
                        + Database.getMilestoneNameFromTicketID(command.ticketID()) + ".");
            case "LOCKED" ->
                error.put("error", "Cannot assign ticket " + command.ticketID() + " from blocked milestone "
                        + Database.getMilestoneNameFromTicketID(command.ticketID()) + ".");
            case "EXPERTISE" ->
                error.put("error", "Developer " + command.username() + " cannot assign ticket " + command.ticketID()
                        + " due to expertise area. Required: "
                        + Database.getTicket(command.ticketID()).getRequiredExpertise() + "; Current: "
                        + ((Developer) Database.getUser(command.username())).getExpertiseArea() + ".");
            default ->
                error.put("error", "Unknown error type: " + errorType);
        }

        outputs.add(error);
    }

    public static void commentError(CommandInput command, String errorType) {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("command", command.command());
        error.put("username", command.username());
        error.put("timestamp", command.timestamp());
        Ticket tkt = Database.getTicket(command.ticketID());
        String message;
        switch (errorType) {
            case "ANON" ->
                message = "Comments are not allowed on anonymous tickets.";
            case "CLOSED" ->
                message = "CLOSED";
            case "MIN_LENGTH" ->
                message = "Comment must be at least 10 characters long.";
            case "ASSIGNMENT_DEVELOPER" ->
                message = "Ticket " + command.ticketID() + " is not assigned to the developer " + command.username()
                        + ".";
            case "ASSIGNMENT_REPORTER" ->
                message = "Reporter " + command.username() + " cannot comment on ticket " + command.ticketID() + ".";
            // case "UNDO" ->
            //     // what happens if we do two undos ? what should happen and what happens
            //     message = tkt.getErrorMessage();
            default ->
                message = "DEFAULT";

        }
        error.put("error", message);


        outputs.add(error);
    }

    public static void milestoneError(CommandInput command, String errorType) {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("command", command.command());
        error.put("username", command.username());
        error.put("timestamp", command.timestamp());

        switch (errorType) {
            case "ANON" ->
                error.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
            case "NUSR" ->
                error.put("error", "The user " + command.username() + " does not exist.");
            case "WRONG_USER_DEVELOPER" ->
                error.put("error",
                        "The user does not have permission to execute this command: required role MANAGER; user role DEVELOPER.");
            case "WRONG_USER_REPORTER" ->
                error.put("error",
                        "The user does not have permission to execute this command: required role MANAGER; user role REPORTER.");
            default -> {
                String[] parts = errorType.split("_");
                error.put("error", "Tickets " + parts[2] + " already assigned to milestone " + parts[1] + ".");
            }
        }

        outputs.add(error);
    }

    public static void ticketError(CommandInput command, String errorType) {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("command", command.command());
        error.put("username", command.username());
        error.put("timestamp", command.timestamp());
        switch (errorType) {
            case "ANON" ->
                error.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
            case "NUSR" ->
                error.put("error", "The user " + command.username() + " does not exist.");
            case "WPER" ->
                error.put("error", "Tickets can only be reported during testing phases.");

            default ->
                error.put("error", "implement");
            // user does not exist
            // only testing period
        }
        outputs.add(error);
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
