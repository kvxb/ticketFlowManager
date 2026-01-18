package main;

import database.Database;
import io.IOUtil;
import io.CommandInput;

import java.io.IOException;
import java.util.List;

import java.time.LocalDate;
import tickets.Ticket;
import users.Developer;

/**
 * Main application class that orchestrates the ticket management system.
 * Contains the core logic for processing commands during testing and
 * development periods.
 */
public final class App {
    /**
     * Private constructor to prevent instantiation.
     */
    private App() {
    }

    private static LocalDate currentDate;
    private static int it = 0;
    private static Database db = Database.getInstance();

    private static final int TESTING_PERIOD_DAYS = 12;
    private static final int DEV_PERIOD_DAYS = 1000;

    /**
     * Processes commands during the testing period (first 12 days).
     * Only handles ticket reporting and viewing commands.
     */
    public static void testingPeriod() {
        final LocalDate endDate = currentDate.plusDays(TESTING_PERIOD_DAYS);

        while (it < db.getSize("commands")) {
            final CommandInput currentCommand = db.getCommands().get(it);
            currentDate = currentCommand.time();
            if (currentDate.isAfter(endDate)) {
                currentDate = endDate;
                break;
            }
            db.update(currentCommand.time());

            switch (currentCommand.command()) {
                case "reportTicket":
                    db.addTicket(currentCommand);
                    break;
                case "viewTickets":
                    IOUtil.viewTickets(currentCommand, db.getTickets(currentCommand.username()));
                    break;
                case "search":
                    IOUtil.outputSearch(currentCommand, db.getSearchResults(currentCommand));
                    break;
                default:
                    System.out.println("didnt match command in testing");
            }
            it++;
        }
    }

    /**
     * Processes commands during the development period (after testing).
     * Handles all system commands including milestone management, assignments, and
     * analytics.
     */
    public static void developPeriod() {
        final LocalDate endDate = currentDate.plusDays(DEV_PERIOD_DAYS);

        while (it < db.getSize("commands")) {
            final CommandInput currentCommand = db.getCommands().get(it);
            currentDate = currentCommand.time();
            if (currentDate.isAfter(endDate)) {
                break;
            }
            db.update(currentCommand.time());

            switch (currentCommand.command()) {
                case "reportTicket":
                    IOUtil.ticketError(currentCommand, "WPER");
                    break;
                case "viewTickets":
                    IOUtil.viewTickets(currentCommand, db.getTickets(currentCommand.username()));
                    break;
                case "createMilestone":
                    db.addMilestone(currentCommand);
                    break;
                case "viewMilestones":
                    IOUtil.viewMilestones(currentCommand,
                            db.getMilestones(currentCommand.username()));
                    break;
                case "assignTicket":
                    db.assignTicket(currentCommand);
                    break;
                case "viewAssignedTickets":
                    IOUtil.viewAssignedTickets(currentCommand,
                            db.getAssignedTickets(currentCommand.username()));
                    break;
                case "undoAssignTicket":
                    db.undoAssignedTicket(currentCommand);
                    break;
                case "addComment":
                    db.addComment(currentCommand);
                    break;
                case "undoAddComment":
                    db.undoAddComment(currentCommand);
                    break;
                case "changeStatus":
                    db.changeStatus(currentCommand);
                    break;
                case "viewTicketHistory":
                    IOUtil.viewTicketHistory(currentCommand,
                            db.getTicketsConcerningUser(currentCommand.username()));
                    break;
                case "undoChangeStatus":
                    db.undoChangeStatus(currentCommand);
                    break;
                case "search":
                    IOUtil.outputSearch(currentCommand, db.getSearchResults(currentCommand));
                    break;
                case "viewNotifications":
                    final Developer dev = (Developer) db.getUser(currentCommand.username());
                    final List<String> notifications = dev.getNotifications();
                    IOUtil.outputNotifications(currentCommand, notifications);
                    dev.clearNotifications();
                    break;
                case "generateCustomerImpactReport":
                    IOUtil.generateCustomerImpactReport(currentCommand, db.getCustomerImpact());
                    break;
                case "generateTicketRiskReport":
                    IOUtil.generateTicketRiskReport(currentCommand, db.getTicketRisk());
                    break;
                case "generateResolutionEfficiencyReport":
                    IOUtil.generateResolutionEfficiencyReport(currentCommand,
                            db.getResolutionEfficiency());
                    break;
                case "appStabilityReport":
                    IOUtil.generateAppStabilityReport(currentCommand, db.getAppStability());
                    break;
                case "generatePerformanceReport":
                    IOUtil.generatePerformanceReport(currentCommand,
                            db.getPerformance(currentCommand));
                    break;
                case "startTestingPhase":
                    testingPeriod();
                    continue;
                default:
            }
            it++;
        }

    }

    /**
     * Main entry point for running the application.
     * Initializes the system, processes commands, and manages the workflow.
     *
     * @param inputPath  Path to the input JSON files
     * @param outputPath Path where output should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        db.clearDatabase();
        IOUtil.clearIO();
        IOUtil.setPaths(inputPath, outputPath);
        Ticket.clearTicket();
        it = 0;

        try {
            db.setUsers(IOUtil.readUsers());
            db.setCommands(IOUtil.readCommands());
        } catch (final IOException e) {
            System.out.println("error reading from input file: " + e.getMessage());
            return;
        }

        currentDate = db.getCommands().getFirst().time();
        db.setLastUpdate(currentDate);

        testingPeriod();
        developPeriod();

        IOUtil.writeOutput();
    }
}
