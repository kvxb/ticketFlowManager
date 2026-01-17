package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.lang.Object;
import database.Database;
import milestones.Milestone;
import users.Developer;
import users.User;
import tickets.Ticket;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

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

    private static ObjectNode createOutputHeader(final CommandInput command) {
        final ObjectNode node = MAPPER.createObjectNode();
        node.put("command", command.command());
        node.put("username", command.username());
        node.put("timestamp", command.timestamp());
        return node;
    }

    public static List<CommandInput> readCommands() throws IOException {
        final File inputFile = new File(inputPath);

        return MAPPER.readerForListOf(CommandInput.class)
                .readValue(inputFile);
    }

    public static List<UserInput> readUsers() throws IOException {
        final File inputFile = new File(db.getUsersDb());

        return MAPPER.readerForListOf(UserInput.class)
                .readValue(inputFile);
    }

    public static void outputSearch(final CommandInput command, final List<?> results) {
        final User user = db.getUser(command.username());

        if (command.filters() == null) {
            System.out.println("ERROR: filters is null for search command!");
            return;
        }
        final ObjectNode commandNode = createOutputHeader(command);
        commandNode.put("searchType", command.filters().searchType());

        final ArrayNode resultsArray = MAPPER.createArrayNode();

        if ("DEVELOPER".equals(command.filters().searchType())) {
            for (final Object obj : results) {
                final Developer dev = (Developer) obj;
                final ObjectNode devNode = MAPPER.createObjectNode();
                devNode.put("username", dev.getUsername());
                devNode.put("expertiseArea", dev.getExpertiseArea().toString());
                devNode.put("seniority", dev.getSeniority().toString());
                devNode.put("performanceScore", dev.getPerformanceScore());
                devNode.put("hireDate", dev.getHireDate().toString());
                resultsArray.add(devNode);
            }
        } else {
            final boolean hasKeywordsFilter = command.filters().keywords() != null &&
                    command.filters().keywords().length > 0;

            for (final Object obj : results) {
                final Ticket ticket = (Ticket) obj;
                final ObjectNode ticketNode = MAPPER.createObjectNode();
                ticketNode.put("id", ticket.getId());
                ticketNode.put("type", ticket.getType().toString());
                ticketNode.put("title", ticket.getTitle());
                ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
                ticketNode.put("status", ticket.getStatus().toString());
                ticketNode.put("createdAt", ticket.getCreatedAt());
                ticketNode.put("solvedAt", ticket.getSolvedAt() != null ? ticket.getSolvedAt() : "");
                ticketNode.put("reportedBy", ticket.getReportedBy());

                if ((hasKeywordsFilter &&
                        ticket.getMatchingWords() != null &&
                        !ticket.getMatchingWords().isEmpty()) || user.getRole().name().equals("MANAGER")) {

                    final ArrayNode matchingWordsArray = MAPPER.createArrayNode();
                    for (final String word : ticket.getMatchingWords()) {
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

    public static void generatePerformanceReport(final CommandInput command, final List<List<Object>> reportData) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode reportArray = MAPPER.createArrayNode();

        for (final List<Object> row : reportData) {
            final ObjectNode devNode = MAPPER.createObjectNode();

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

    public static void outputNotifications(final CommandInput command, final List<String> notifications) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode notificationsArray = MAPPER.createArrayNode();
        for (final String notification : notifications) {
            notificationsArray.add(notification);
        }

        commandNode.set("notifications", notificationsArray);
        outputs.add(commandNode);
    }

    public static void viewAssignedTickets(final CommandInput command, final List<Ticket> tickets) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode ticketsArray = MAPPER.createArrayNode();

        for (final Ticket ticket : tickets) {
            final ObjectNode ticketNode = MAPPER.createObjectNode();

            ticketNode.put("id", ticket.getId());
            ticketNode.put("type", ticket.getType());
            ticketNode.put("title", ticket.getTitle());
            ticketNode.put("businessPriority", ticket.getBusinessPriority().toString());
            ticketNode.put("status", ticket.getStatus().toString());

            ticketNode.put("createdAt", ticket.getCreatedAt() != null ? ticket.getCreatedAt() : "");
            ticketNode.put("assignedAt", ticket.getAssignedAt() != null ? ticket.getAssignedAt() : "");
            ticketNode.put("reportedBy", ticket.getReportedBy() != null ? ticket.getReportedBy() : "");

            final ArrayNode commentsArray = MAPPER.createArrayNode();
            ticket.getComments().stream()
                    .forEach(comment -> {
                        final ObjectNode commentNode = MAPPER.createObjectNode();
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

    public static void generateAppStabilityReport(final CommandInput command, final List<Object> reportData) {
        final ObjectNode commandNode = createOutputHeader(command);
        final ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalOpenTickets", ((Number) reportData.get(0)).intValue());

        final ObjectNode openTicketsByTypeNode = MAPPER.createObjectNode();
        openTicketsByTypeNode.put("BUG", ((Number) reportData.get(1)).intValue());
        openTicketsByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(2)).intValue());
        openTicketsByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(3)).intValue());
        reportNode.set("openTicketsByType", openTicketsByTypeNode);

        final ObjectNode openTicketsByPriorityNode = MAPPER.createObjectNode();
        openTicketsByPriorityNode.put("LOW", ((Number) reportData.get(4)).intValue());
        openTicketsByPriorityNode.put("MEDIUM", ((Number) reportData.get(5)).intValue());
        openTicketsByPriorityNode.put("HIGH", ((Number) reportData.get(6)).intValue());
        openTicketsByPriorityNode.put("CRITICAL", ((Number) reportData.get(7)).intValue());
        reportNode.set("openTicketsByPriority", openTicketsByPriorityNode);

        final ObjectNode riskByTypeNode = MAPPER.createObjectNode();
        riskByTypeNode.put("BUG", (String) reportData.get(8));
        riskByTypeNode.put("FEATURE_REQUEST", (String) reportData.get(9));
        riskByTypeNode.put("UI_FEEDBACK", (String) reportData.get(10));
        reportNode.set("riskByType", riskByTypeNode);

        final ObjectNode impactByTypeNode = MAPPER.createObjectNode();
        impactByTypeNode.put("BUG", ((Number) reportData.get(11)).doubleValue());
        impactByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(12)).doubleValue());
        impactByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(13)).doubleValue());
        reportNode.set("impactByType", impactByTypeNode);

        reportNode.put("appStability", (String) reportData.get(14));

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateTicketRiskReport(final CommandInput command, final List<Object> reportData) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", ((Number) reportData.get(0)).intValue());

        final ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", ((Number) reportData.get(1)).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", ((Number) reportData.get(2)).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", ((Number) reportData.get(3)).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        final ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", ((Number) reportData.get(4)).intValue());
        ticketsByPriorityNode.put("MEDIUM", ((Number) reportData.get(5)).intValue());
        ticketsByPriorityNode.put("HIGH", ((Number) reportData.get(6)).intValue());
        ticketsByPriorityNode.put("CRITICAL", ((Number) reportData.get(7)).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        final ObjectNode riskByTypeNode = MAPPER.createObjectNode();
        riskByTypeNode.put("BUG", (String) reportData.get(8));
        riskByTypeNode.put("FEATURE_REQUEST", (String) reportData.get(9));
        riskByTypeNode.put("UI_FEEDBACK", (String) reportData.get(10));
        reportNode.set("riskByType", riskByTypeNode);

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateResolutionEfficiencyReport(final CommandInput command, final List<Number> reportData) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", reportData.get(0).intValue());

        final ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", reportData.get(1).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", reportData.get(2).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", reportData.get(3).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        final ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", reportData.get(4).intValue());
        ticketsByPriorityNode.put("MEDIUM", reportData.get(5).intValue());
        ticketsByPriorityNode.put("HIGH", reportData.get(6).intValue());
        ticketsByPriorityNode.put("CRITICAL", reportData.get(7).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        final ObjectNode customerImpactByTypeNode = MAPPER.createObjectNode();
        customerImpactByTypeNode.put("BUG", reportData.get(8).doubleValue());
        customerImpactByTypeNode.put("FEATURE_REQUEST", reportData.get(9).doubleValue());
        customerImpactByTypeNode.put("UI_FEEDBACK", reportData.get(10).doubleValue());
        reportNode.set("efficiencyByType", customerImpactByTypeNode);

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void generateCustomerImpactReport(final CommandInput command, final List<Number> reportData) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ObjectNode reportNode = MAPPER.createObjectNode();

        reportNode.put("totalTickets", reportData.get(0).intValue());

        final ObjectNode ticketsByTypeNode = MAPPER.createObjectNode();
        ticketsByTypeNode.put("BUG", reportData.get(1).intValue());
        ticketsByTypeNode.put("FEATURE_REQUEST", reportData.get(2).intValue());
        ticketsByTypeNode.put("UI_FEEDBACK", reportData.get(3).intValue());
        reportNode.set("ticketsByType", ticketsByTypeNode);

        final ObjectNode ticketsByPriorityNode = MAPPER.createObjectNode();
        ticketsByPriorityNode.put("LOW", reportData.get(4).intValue());
        ticketsByPriorityNode.put("MEDIUM", reportData.get(5).intValue());
        ticketsByPriorityNode.put("HIGH", reportData.get(6).intValue());
        ticketsByPriorityNode.put("CRITICAL", reportData.get(7).intValue());
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        final ObjectNode customerImpactByTypeNode = MAPPER.createObjectNode();
        customerImpactByTypeNode.put("BUG", reportData.get(8).doubleValue());
        customerImpactByTypeNode.put("FEATURE_REQUEST", reportData.get(9).doubleValue());
        customerImpactByTypeNode.put("UI_FEEDBACK", reportData.get(10).doubleValue());
        reportNode.set("customerImpactByType", customerImpactByTypeNode);

        commandNode.set("report", reportNode);
        outputs.add(commandNode);
    }

    public static void viewTickets(final CommandInput command, final List<Ticket> tickets) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode ticketsArray = MAPPER.createArrayNode();

        for (final Ticket ticket : tickets) {
            final ObjectNode ticketNode = MAPPER.createObjectNode();

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

            final ArrayNode commentsArray = MAPPER.createArrayNode();
            ticket.getComments().stream()
                    .forEach(comment -> {
                        final ObjectNode commentNode = MAPPER.createObjectNode();
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

    public static void viewMilestones(final CommandInput command, final List<Milestone> unsortedMilestones) {
        final List<Milestone> sortedMilestones = unsortedMilestones.stream()
                .sorted(Comparator
                        .comparing(Milestone::getDueDate)
                        .thenComparing(Milestone::getName))
                .collect(Collectors.toList());

        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode milestonesArray = MAPPER.createArrayNode();

        for (final Milestone milestone : sortedMilestones) {
            final ObjectNode ticketNode = MAPPER.createObjectNode();

            ticketNode.put("name", milestone.getName());
            final ArrayNode blockingFor = MAPPER.createArrayNode();
            Arrays.stream(milestone.getBlockingFor())
                    .forEach(blockingFor::add);
            ticketNode.set("blockingFor", blockingFor);
            ticketNode.put("dueDate", milestone.getDueDate());
            ticketNode.put("createdAt", milestone.getCreatedAt());
            final ArrayNode tickets = MAPPER.createArrayNode();
            Arrays.stream(milestone.getTickets())
                    .forEach(tickets::add);
            ticketNode.set("tickets", tickets);
            final ArrayNode assignedDevs = MAPPER.createArrayNode();
            Arrays.stream(milestone.getAssignedDevs())
                    .forEach(assignedDevs::add);
            ticketNode.set("assignedDevs", assignedDevs);

            ticketNode.put("createdBy", milestone.getOwner());
            ticketNode.put("status", milestone.getStatus());
            ticketNode.put("isBlocked", milestone.isBlocked());
            ticketNode.put("daysUntilDue", milestone.getDaysUntilDue());

            ticketNode.put("overdueBy", milestone.getOverdueBy());
            final ArrayNode openTickets = MAPPER.createArrayNode();
            milestone.getOpenTickets().stream()
                    .sorted()
                    .forEach(openTickets::add);
            ticketNode.set("openTickets", openTickets);
            final ArrayNode closedTickets = MAPPER.createArrayNode();
            milestone.getClosedTickets().stream()
                    .sorted()
                    .forEach(closedTickets::add);
            ticketNode.set("closedTickets", closedTickets);
            ticketNode.put("completionPercentage", milestone.getCompletionPercentage());

            final ArrayNode repartition = Arrays.stream(milestone.getRepartitions())
                    .filter(rep -> rep != null)
                    .filter(rep -> rep.getDev() != null)
                    .map(rep -> {
                        ObjectNode devNode = MAPPER.createObjectNode();
                        devNode.put("developer", rep.getDev());

                        ArrayNode assignedArray = MAPPER.createArrayNode();

                        if (rep.getAssignedTickets() != null) {
                            rep.getAssignedTickets().stream().sorted().forEach(assignedArray::add);
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

    public static void viewTicketHistory(final CommandInput command, final List<Ticket> userTickets) {
        final ObjectNode commandNode = createOutputHeader(command);

        final ArrayNode ticketHistoryArray = MAPPER.createArrayNode();

        if (userTickets == null) {
            commandNode.put("error",
                    "The user does not have permission to execute this command: required role DEVELOPER, MANAGER; user role REPORTER.");
        } else {
            for (final Ticket ticket : userTickets) {
                final ObjectNode ticketNode = MAPPER.createObjectNode();
                ticketNode.put("id", ticket.getId());
                ticketNode.put("title", ticket.getTitle());
                ticketNode.put("status", ticket.getStatus().toString());

                final ArrayNode actionsArray = MAPPER.createArrayNode();

                if (ticket.getTicketHistory() != null && ticket.getTicketHistory().getActions() != null) {
                    boolean stopOutput = false;

                    for (final Ticket.Action action : ticket.getTicketHistory().getActions()) {
                        if (stopOutput) {
                            break;
                        }

                        if ("DE-ASSIGNED".equals(action.getAction()) &&
                                command.username().equals(action.getBy())) {
                            final ObjectNode actionNode = MAPPER.createObjectNode();

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

                            stopOutput = true;
                            continue;
                        }

                        final ObjectNode actionNode = MAPPER.createObjectNode();

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

                final ArrayNode commentsArray = MAPPER.createArrayNode();

                if (ticket.getComments() != null) {
                    final User user = db.getUser(command.username());

                    if (user.getRole().name().equals("MANAGER")) {
                        for (final Ticket.Comment comment : ticket.getComments()) {
                            final ObjectNode commentNode = MAPPER.createObjectNode();
                            commentNode.put("author", comment.getAuthor());
                            commentNode.put("content", comment.getContent());
                            commentNode.put("createdAt", comment.getCreatedAt());
                            commentsArray.add(commentNode);
                        }
                    } else if (user.getRole().name().equals("DEVELOPER")) {
                        final Developer currentDev = (Developer) user;
                        final int allowedCommentCount = currentDev.getCommentCountForTicket(ticket.getId());
                        final List<Ticket.Comment> sortedComments = new ArrayList<>(ticket.getComments());
                        sortedComments.sort(Comparator.comparing(Ticket.Comment::getCreatedAt));

                        for (int i = 0; i < Math.min(allowedCommentCount, sortedComments.size()); i++) {
                            final Ticket.Comment comment = sortedComments.get(i);
                            final ObjectNode commentNode = MAPPER.createObjectNode();
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
        }
        outputs.add(commandNode);
    }

    public static void assignError(final CommandInput command, final String errorType) {
        final ObjectNode error = createOutputHeader(command);

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

    public static void commentError(final CommandInput command, final String errorType) {
        final ObjectNode error = createOutputHeader(command);
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
            default ->
                message = "DEFAULT";
        }
        error.put("error", message);

        outputs.add(error);
    }

    public static void milestoneError(final CommandInput command, final String errorType) {
        final ObjectNode error = createOutputHeader(command);

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
                final String[] parts = errorType.split("_");
                error.put("error", "Tickets " + parts[2] + " already assigned to milestone " + parts[1] + ".");
            }
        }

        outputs.add(error);
    }

    public static void changeError(final CommandInput command, final String errorType) {
        final ObjectNode error = createOutputHeader(command);

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

    public static void ticketError(final CommandInput command, final String errorType) {
        final ObjectNode error = createOutputHeader(command);
        switch (errorType) {
            case "ANON" ->
                error.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
            case "NUSR" ->
                error.put("error", "The user " + command.username() + " does not exist.");
            case "WPER" ->
                error.put("error", "Tickets can only be reported during testing phases.");

            default ->
                error.put("error", "implement");
        }
        outputs.add(error);
    }

    public static void writeOutput() {
        try {
            final File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (final IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }

    public static void setInputPath(final String path) {
        inputPath = path;
    }

    public static void setOutputPath(final String path) {
        outputPath = path;
    }

    public static void setPaths(final String input, final String output) {
        setInputPath(input);
        setOutputPath(output);
    }
}
