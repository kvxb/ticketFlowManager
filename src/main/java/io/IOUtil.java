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
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import tickets.Ticket.TicketHistory;

public class IOUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static List<ObjectNode> outputs = new ArrayList<>();
    private static String inputPath;
    private static String outputPath;
    private static Database db = Database.getInstance();

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
        File inputFile = new File(db.getUsersDb());

        return MAPPER.readerForListOf(UserInput.class)
                .readValue(inputFile);
    }

    public static void outputSearch(CommandInput command, List<?> results) {
        if (command.filters() == null) {
            System.out.println("ERROR: filters is null for search command!");
            return;
        }
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());
        commandNode.put("searchType", command.filters().searchType());

        ArrayNode resultsArray = MAPPER.createArrayNode();

        if ("DEVELOPER".equals(command.filters().searchType())) {
            for (Object obj : results) {
                Developer dev = (Developer) obj;
                ObjectNode devNode = MAPPER.createObjectNode();
                devNode.put("username", dev.getUsername());
                devNode.put("expertiseArea", dev.getExpertiseArea().toString());
                devNode.put("seniority", dev.getSeniority().toString());
                devNode.put("performanceScore", dev.getPerformanceScore());
                devNode.put("hireDate", dev.getHireDate().toString());
                resultsArray.add(devNode);
            }
        } else {
            boolean hasKeywordsFilter = command.filters().keywords() != null &&
                    command.filters().keywords().length > 0;

            for (Object obj : results) {
                Ticket ticket = (Ticket) obj;
                ObjectNode ticketNode = MAPPER.createObjectNode();
                ticketNode.put("id", ticket.getId());
                ticketNode.put("type", ticket.getType().toString());
                ticketNode.put("title", ticket.getTitle());
                ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
                ticketNode.put("status", ticket.getStatus().toString());
                ticketNode.put("createdAt", ticket.getCreatedAt());
                ticketNode.put("solvedAt", ticket.getSolvedAt() != null ? ticket.getSolvedAt() : "");
                ticketNode.put("reportedBy", ticket.getReportedBy());

                if (hasKeywordsFilter &&
                        ticket.getMatchingWords() != null &&
                        !ticket.getMatchingWords().isEmpty()) {

                    ArrayNode matchingWordsArray = MAPPER.createArrayNode();
                    for (String word : ticket.getMatchingWords()) {
                        matchingWordsArray.add(word);
                    }
                    ticketNode.set("matchingWords", matchingWordsArray);
                }

                resultsArray.add(ticketNode);
            }
        }

        commandNode.set("results", resultsArray);
        outputs.add(commandNode);
    }

    public static void generatePerformanceReport(CommandInput command, List<List<Object>> reportData) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ArrayNode reportArray = MAPPER.createArrayNode();

        for (List<Object> row : reportData) {
            ObjectNode devNode = MAPPER.createObjectNode();

            devNode.put("username", (String) row.get(0));
            devNode.put("closedTickets", ((Number) row.get(1)).intValue());
            devNode.put("averageResolutionTime", ((Number) row.get(2)).doubleValue());
            devNode.put("performanceScore", ((Number) row.get(3)).doubleValue());
            devNode.put("seniority", (String) row.get(4));

            reportArray.add(devNode);
        }

        commandNode.set("report", reportArray);
        outputs.add(commandNode);
    }

    public static void outputNotifications(CommandInput command, List<String> notifications) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ArrayNode notificationsArray = MAPPER.createArrayNode();
        for (String notification : notifications) {
            notificationsArray.add(notification);
        }

        commandNode.set("notifications", notificationsArray);
        outputs.add(commandNode);
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

    public static void generateAppStabilityReport(CommandInput command, List<Object> reportData) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalOpenTickets", ((Number) reportData.get(0)).intValue());

        ObjectNode openTicketsByTypeNode = MAPPER.createObjectNode();
        openTicketsByTypeNode.put("BUG", ((Number) reportData.get(1)).intValue());
        openTicketsByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(2)).intValue());
        openTicketsByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(3)).intValue());
        reportNode.set("openTicketsByType", openTicketsByTypeNode);

        ObjectNode openTicketsByPriorityNode = MAPPER.createObjectNode();
        openTicketsByPriorityNode.put("LOW", ((Number) reportData.get(4)).intValue());
        openTicketsByPriorityNode.put("MEDIUM", ((Number) reportData.get(5)).intValue());
        openTicketsByPriorityNode.put("HIGH", ((Number) reportData.get(6)).intValue());
        openTicketsByPriorityNode.put("CRITICAL", ((Number) reportData.get(7)).intValue());
        reportNode.set("openTicketsByPriority", openTicketsByPriorityNode);

        ObjectNode riskByTypeNode = MAPPER.createObjectNode();
        riskByTypeNode.put("BUG", (String) reportData.get(8));
        riskByTypeNode.put("FEATURE_REQUEST", (String) reportData.get(9));
        riskByTypeNode.put("UI_FEEDBACK", (String) reportData.get(10));
        reportNode.set("riskByType", riskByTypeNode);

        ObjectNode impactByTypeNode = MAPPER.createObjectNode();
        impactByTypeNode.put("BUG", ((Number) reportData.get(11)).doubleValue());
        impactByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(12)).doubleValue());
        impactByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(13)).doubleValue());
        reportNode.set("impactByType", impactByTypeNode);

        reportNode.put("appStability", (String) reportData.get(14));

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateTicketRiskReport(CommandInput command, List<Object> reportData) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", ((Number) reportData.get(0)).intValue());

        ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", ((Number) reportData.get(1)).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(2)).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(3)).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", ((Number) reportData.get(4)).intValue());
        ticketsByPriorityNode.put("MEDIUM", ((Number) reportData.get(5)).intValue());
        ticketsByPriorityNode.put("HIGH", ((Number) reportData.get(6)).intValue());
        ticketsByPriorityNode.put("CRITICAL", ((Number) reportData.get(7)).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        ObjectNode riskByTypeNode = MAPPER.createObjectNode();
        riskByTypeNode.put("BUG", (String) reportData.get(8));
        riskByTypeNode.put("FEATURE_REQUEST", (String) reportData.get(9));
        riskByTypeNode.put("UI_FEEDBACK", (String) reportData.get(10));
        reportNode.set("riskByType", riskByTypeNode);

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateResolutionEfficiencyReport(CommandInput command, List<Number> reportData) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", reportData.get(0).intValue());

        ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", reportData.get(1).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", reportData.get(2).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", reportData.get(3).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", reportData.get(4).intValue());
        ticketsByPriorityNode.put("MEDIUM", reportData.get(5).intValue());
        ticketsByPriorityNode.put("HIGH", reportData.get(6).intValue());
        ticketsByPriorityNode.put("CRITICAL", reportData.get(7).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        ObjectNode customerImpactByTypeNode = MAPPER.createObjectNode();
        customerImpactByTypeNode.put("BUG", reportData.get(8).doubleValue());
        customerImpactByTypeNode.put("FEATURE_REQUEST", reportData.get(9).doubleValue());
        customerImpactByTypeNode.put("UI_FEEDBACK", reportData.get(10).doubleValue());
        reportNode.set("efficiencyByType", customerImpactByTypeNode);

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateCustomerImpactReport(CommandInput command, List<Number> reportData) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", reportData.get(0).intValue());

        ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", reportData.get(1).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", reportData.get(2).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", reportData.get(3).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", reportData.get(4).intValue());
        ticketsByPriorityNode.put("MEDIUM", reportData.get(5).intValue());
        ticketsByPriorityNode.put("HIGH", reportData.get(6).intValue());
        ticketsByPriorityNode.put("CRITICAL", reportData.get(7).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        ObjectNode customerImpactByTypeNode = MAPPER.createObjectNode();
        customerImpactByTypeNode.put("BUG", reportData.get(8).doubleValue());
        customerImpactByTypeNode.put("FEATURE_REQUEST", reportData.get(9).doubleValue());
        customerImpactByTypeNode.put("UI_FEEDBACK", reportData.get(10).doubleValue());
        reportNode.set("customerImpactByType", customerImpactByTypeNode);

        commandNode.set("report", reportNode);
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
        // TODO move these to the db.function and give them to IOUTIL
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
            milestone.getOpenTickets().stream()
                    .forEach(openTickets::add);
            ticketNode.set("openTickets", openTickets);
            ArrayNode closedTickets = MAPPER.createArrayNode();
            milestone.getClosedTickets().stream()
                    .forEach(closedTickets::add);
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

    public static void viewTicketHistory(CommandInput command, List<Ticket> userTickets) {
        ObjectNode commandNode = MAPPER.createObjectNode();
        commandNode.put("command", command.command());
        commandNode.put("username", command.username());
        commandNode.put("timestamp", command.timestamp());

        ArrayNode ticketHistoryArray = MAPPER.createArrayNode();

        for (Ticket ticket : userTickets) {
            String deassignTimestamp = null;

            if (ticket.getTicketHistory() != null) {
                for (Ticket.Action action : ticket.getTicketHistory().getActions()) {
                    if ("DE-ASSIGNED".equals(action.getAction()) &&
                            command.username().equals(action.getBy())) {
                        deassignTimestamp = action.getTimestamp();
                    }
                }
            }

            ObjectNode ticketNode = MAPPER.createObjectNode();
            ticketNode.put("id", ticket.getId());
            ticketNode.put("title", ticket.getTitle());
            ticketNode.put("status", ticket.getStatus().toString());

            ArrayNode actionsArray = MAPPER.createArrayNode();

            if (ticket.getTicketHistory() != null && ticket.getTicketHistory().getActions() != null) {
                for (Ticket.Action action : ticket.getTicketHistory().getActions()) {
                    boolean includeAction = true;

                    if (deassignTimestamp != null) {
                        int timestampComparison = action.getTimestamp().compareTo(deassignTimestamp);

                        if (timestampComparison > 0) {
                            includeAction = false;
                        } else if (timestampComparison == 0) {
                            if (!"DE-ASSIGNED".equals(action.getAction())) {
                                includeAction = false;
                            }
                        }
                    }

                    if (!includeAction) {
                        continue;
                    }

                    ObjectNode actionNode = MAPPER.createObjectNode();

                    if (action.getMilestone() != null && !action.getMilestone().isEmpty()) {
                        actionNode.put("milestone", action.getMilestone());
                    }

                    if (action.getFrom() != null) {
                        actionNode.put("from", action.getFrom().toString());
                    }

                    if (action.getTo() != null) {
                        actionNode.put("to", action.getTo().toString());
                    }

                    if (action.getBy() != null && !action.getBy().isEmpty()) {
                        actionNode.put("by", action.getBy());
                    }

                    actionNode.put("timestamp", action.getTimestamp());
                    actionNode.put("action", action.getAction());

                    actionsArray.add(actionNode);
                }
            }

            ticketNode.set("actions", actionsArray);

            ArrayNode commentsArray = MAPPER.createArrayNode();

            if (ticket.getComments() != null) {
                for (Ticket.Comment comment : ticket.getComments()) {
                    if (deassignTimestamp == null ||
                            comment.getCreatedAt().compareTo(deassignTimestamp) < 0) {

                        ObjectNode commentNode = MAPPER.createObjectNode();
                        commentNode.put("author", comment.getAuthor());
                        commentNode.put("content", comment.getContent());
                        commentNode.put("createdAt", comment.getCreatedAt());
                        commentsArray.add(commentNode);
                    }
                }
            }

            ticketNode.set("comments", commentsArray);
            ticketHistoryArray.add(ticketNode);
        }

        commandNode.set("ticketHistory", ticketHistoryArray);
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
                        + db.getTicket(command.ticketID()).getRequiredSeniority() + "; Current: "
                        + ((Developer) db.getUser(command.username())).getSeniority() + ".");
            case "ASSIGNMENT" ->
                error.put("error", "Developer " + command.username() + " is not assigned to milestone "
                        + db.getMilestoneNameFromTicketID(command.ticketID()) + ".");
            case "LOCKED" ->
                error.put("error", "Cannot assign ticket " + command.ticketID() + " from blocked milestone "
                        + db.getMilestoneNameFromTicketID(command.ticketID()) + ".");
            case "EXPERTISE" ->
                error.put("error", "Developer " + command.username() + " cannot assign ticket " + command.ticketID()
                        + " due to expertise area. Required: "
                        + db.getTicket(command.ticketID()).getRequiredExpertise() + "; Current: "
                        + ((Developer) db.getUser(command.username())).getExpertiseArea() + ".");
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
        // Ticket tkt = db.getTicket(command.ticketID());
        String message;
        switch (errorType) {
            case "ANON" ->
                message = "Comments are not allowed on anonymous tickets.";
            case "CLOSED" ->
                message = "Reporters cannot comment on CLOSED tickets.";
            case "MIN_LENGTH" ->
                message = "Comment must be at least 10 characters long.";
            case "ASSIGNMENT_DEVELOPER" ->
                message = "Ticket " + command.ticketID() + " is not assigned to the developer " + command.username()
                        + ".";
            case "ASSIGNMENT_REPORTER" ->
                message = "Reporter " + command.username() + " cannot comment on ticket " + command.ticketID() + ".";
            // case "UNDO" ->
            // // what happens if we do two undos ? what should happen and what happens
            // message = tkt.getErrorMessage();
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

    public static void changeError(CommandInput command, String errorType) {
        ObjectNode error = MAPPER.createObjectNode();
        error.put("command", command.command());
        error.put("username", command.username());
        error.put("timestamp", command.timestamp());

        switch (errorType) {
            case "ASSIGNMENT" ->
                error.put("error",
                        "Ticket " + command.ticketID() + " is not assigned to developer " + command.username() + ".");
            default -> {
                error.put("error", "DEFAULT");
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
