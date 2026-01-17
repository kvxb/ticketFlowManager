// package main;
//
// import database.Database;
// import io.CommandInput;
// import io.IOUtil;
// import users.Developer;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// public class CommandDispatcher {
//     public enum Phase { TESTING, DEVELOPMENT }
//
//     private Phase currentPhase = Phase.TESTING;
//     private final Database db;
//     private final Map<String, CommandHandler> handlers = new HashMap<>();
//
//     public CommandDispatcher(Database db) {
//         this.db = db;
//         registerAllHandlers();
//     }
//
//     private void registerAllHandlers() {
//         // ===== TESTING-ONLY COMMANDS =====
//         handlers.put("reportTicket", (input, dbInstance) -> {
//             if (currentPhase == Phase.TESTING) {
//                 dbInstance.addTicket(input);
//             } else {
//                 IOUtil.ticketError(input, "WPER");
//             }
//         });
//
//         // ===== DEVELOPMENT-ONLY COMMANDS =====
//         handlers.put("createMilestone", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.addMilestone(input);
//             }
//         });
//
//         handlers.put("assignTicket", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.assignTicket(input);
//             }
//         });
//
//         handlers.put("viewAssignedTickets", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.viewAssignedTickets(input, dbInstance.getAssignedTickets(input.username()));
//             }
//         });
//
//         handlers.put("undoAssignTicket", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.undoAssignedTicket(input);
//             }
//         });
//
//         handlers.put("addComment", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.addComment(input);
//             }
//         });
//
//         handlers.put("undoAddComment", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.undoAddComment(input);
//             }
//         });
//
//         handlers.put("changeStatus", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.changeStatus(input);
//             }
//         });
//
//         handlers.put("undoChangeStatus", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 dbInstance.undoChangeStatus(input);
//             }
//         });
//
//         handlers.put("viewNotifications", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 Developer dev = (Developer) dbInstance.getUser(input.username());
//                 List<String> notifications = dev.getNotifications();
//                 IOUtil.outputNotifications(input, notifications);
//                 dev.clearNotifications();
//             }
//         });
//
//         handlers.put("generateCustomerImpactReport", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.generateCustomerImpactReport(input, dbInstance.getCustomerImpact());
//             }
//         });
//
//         handlers.put("generateTicketRiskReport", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.generateTicketRiskReport(input, dbInstance.getTicketRisk());
//             }
//         });
//
//         handlers.put("generateResolutionEfficiencyReport", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.generateResolutionEfficiencyReport(input, dbInstance.getResolutionEfficiency());
//             }
//         });
//
//         handlers.put("appStabilityReport", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.generateAppStabilityReport(input, dbInstance.getAppStability());
//             }
//         });
//
//         handlers.put("generatePerformanceReport", (input, dbInstance) -> {
//             if (currentPhase == Phase.DEVELOPMENT) {
//                 IOUtil.generatePerformanceReport(input, dbInstance.getPerformance(input));
//             }
//         });
//
//         // ===== CROSS-PHASE COMMANDS =====
//         handlers.put("viewTickets", (input, dbInstance) -> {
//             IOUtil.viewTickets(input, dbInstance.getTickets(input.username()));
//         });
//
//         handlers.put("search", (input, dbInstance) -> {
//             IOUtil.outputSearch(input, dbInstance.getSearchResults(input));
//         });
//
//         handlers.put("viewMilestones", (input, dbInstance) -> {
//             IOUtil.viewMilestones(input, dbInstance.getMilestones(input.username()));
//         });
//
//         handlers.put("viewTicketHistory", (input, dbInstance) -> {
//             IOUtil.viewTicketHistory(input, dbInstance.getTicketsConcerningUser(input.username()));
//         });
//
//         // ===== SPECIAL COMMAND =====
//         handlers.put("startTestingPhase", (input, dbInstance) -> {
//             System.out.println("startTestingPhase received - should switch to testing period");
//         });
//     }
//
//     public void dispatch(CommandInput input) {
//         CommandHandler handler = handlers.get(input.command());
//         if (handler != null) {
//             handler.handle(input, db);
//         } else {
//             System.out.println("Unknown command: " + input.command());
//         }
//     }
//
//     public void setPhase(Phase phase) {
//         this.currentPhase = phase;
//     }
//
//     public Phase getCurrentPhase() {
//         return currentPhase;
//     }
// }
