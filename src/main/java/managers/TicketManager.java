// package managers;
//
// import tickets.Ticket;
// import tickets.Bug;
// import tickets.FeatureRequest;
// import tickets.UIFeedback;
// import tickets.Ticket.TicketHistory;
// import tickets.Ticket.Status;
// import io.CommandInput;
// import io.IOUtil;
// import io.FiltersInput;
// import users.User;
// import users.Developer;
// import milestones.Milestone;
// import search.filters.FilterContext;
// import search.filters.impl.BusinessPriorityFilter;
// import search.filters.impl.TypeFilter;
// import search.filters.impl.CreatedAtFilter;
// import search.filters.impl.CreatedBeforeFilter;
// import search.filters.impl.CreatedAfterFilter;
// import search.filters.impl.KeywordsFilter;
// import search.filters.impl.AvailableForAssignmentFilter;
//
// import java.util.*;
// import java.util.stream.Collectors;
//
// public class TicketManager {
//     private final List<Ticket> tickets = new ArrayList<>();
//     private Database database; // Referință către Database pentru acces la alte manageri
//
//     public TicketManager() {
//         // Constructor
//     }
//
//     public void setDatabase(Database database) {
//         this.database = database;
//     }
//
//     // ==================== METODE BASIC ====================
//
//     public int getSize() {
//         return tickets.size();
//     }
//
//     public void clearTickets() {
//         tickets.clear();
//     }
//
//     public Ticket getTicket(int id) {
//         for (final Ticket t : tickets) {
//             if (t.getId() == id) {
//                 return t;
//             }
//         }
//         return null;
//     }
//
//     public List<Ticket> getAllTickets() {
//         return new ArrayList<>(tickets);
//     }
//
//     public void setTickets(List<Ticket> tickets) {
//         this.tickets.clear();
//         this.tickets.addAll(tickets);
//     }
//
//     // ==================== METODE DE BUSINESS ====================
//
//     public void createTicketFromCommand(final CommandInput command) {
//         tickets.add(
//                 switch (command.params().type()) {
//                     case "BUG" -> {
//                         final Bug bug = new Bug.Builder()
//                                 .id(Ticket.getTicketId())
//                                 .title(command.params().title())
//                                 .type(command.params().type())
//                                 .businessPriority(
//                                         command.params().reportedBy().isEmpty()
//                                                 ? Ticket.BusinessPriority.LOW
//                                                 : Ticket.BusinessPriority.valueOf(
//                                                         command.params().businessPriority().toUpperCase()))
//                                 .expertiseArea(Ticket.ExpertiseArea.valueOf(
//                                         command.params().expertiseArea().toUpperCase()))
//                                 .reportedBy(command.params().reportedBy())
//                                 .expectedBehaviour(command.params().expectedBehavior())
//                                 .actualBehaviour(command.params().actualBehavior())
//                                 .frequency(Bug.Frequency.valueOf(
//                                         command.params().frequency().toUpperCase()))
//                                 .severity(Bug.Severity.valueOf(
//                                         command.params().severity().toUpperCase()))
//                                 .environment(command.params().environment())
//                                 .errorCode(command.params().errorCode() != null
//                                         ? Integer.parseInt(command.params().errorCode())
//                                         : 0)
//                                 .createdAt(command.timestamp())
//                                 .build();
//                         yield bug;
//                     }
//                     case "FEATURE_REQUEST" -> {
//                         final FeatureRequest fr = new FeatureRequest.Builder()
//                                 .id(Ticket.getTicketId())
//                                 .type(command.params().type())
//                                 .title(command.params().title())
//                                 .businessPriority(Ticket.BusinessPriority.valueOf(
//                                         command.params().businessPriority().toUpperCase()))
//                                 .expertiseArea(Ticket.ExpertiseArea.valueOf(
//                                         command.params().expertiseArea().toUpperCase()))
//                                 .reportedBy(command.params().reportedBy())
//                                 .businessValue(FeatureRequest.BusinessValue.valueOf(
//                                         command.params().businessValue().toUpperCase()))
//                                 .customerDemand(FeatureRequest.CustomerDemand.valueOf(
//                                         command.params().customerDemand().toUpperCase()))
//                                 .createdAt(command.timestamp())
//                                 .build();
//                         yield fr;
//                     }
//                     case "UI_FEEDBACK" -> {
//                         final UIFeedback ui = new UIFeedback.Builder()
//                                 .id(Ticket.getTicketId())
//                                 .type(command.params().type())
//                                 .title(command.params().title())
//                                 .businessPriority(Ticket.BusinessPriority.valueOf(
//                                         command.params().businessPriority().toUpperCase()))
//                                 .expertiseArea(Ticket.ExpertiseArea.valueOf(
//                                         command.params().expertiseArea().toUpperCase()))
//                                 .reportedBy(command.params().reportedBy())
//                                 .businessValue(FeatureRequest.BusinessValue.valueOf(
//                                         command.params().businessValue().toUpperCase()))
//                                 .uiElementId(command.params().uiElementId())
//                                 .usabilityScore(command.params().usabilityScore())
//                                 .screenshotUrl(command.params().screenshotUrl())
//                                 .suggestedFix(command.params().suggestedFix())
//                                 .createdAt(command.timestamp())
//                                 .build();
//                         yield ui;
//                     }
//                     default -> throw new IllegalArgumentException(
//                             "Unknown ticket type: " + command.params().type());
//                 });
//         Ticket.setTicketId(Ticket.getTicketId() + 1);
//     }
//
//     public List<Ticket> getAssignedTickets(final String username) {
//         return tickets.stream()
//                 .filter(ticket -> ticket.getAssignedTo() != null &&
//                         ticket.getAssignedTo().equals(username))
//                 .sorted(Comparator
//                         .comparing(Ticket::getBusinessPriority).reversed()
//                         .thenComparing(Ticket::getId))
//                 .collect(Collectors.toList());
//     }
//
//     public List<Ticket> getTicketsForReporter(final String username) {
//         return tickets.stream()
//                 .filter(ticket -> username.equals(ticket.getReportedBy()))
//                 .collect(Collectors.toList());
//     }
//
//     public List<Ticket> getTicketsConcerningUser(final String username,
//             final User user,
//             final List<Milestone> milestones) {
//         final List<Ticket> filteredTickets = new ArrayList<>();
//
//         if (user.getRole().name().equals("REPORTER")) {
//             return null;
//         }
//
//         for (final Ticket ticket : tickets) {
//             boolean shouldAddTicket = false;
//
//             switch (user.getRole().name()) {
//                 case "DEVELOPER":
//                     if (ticket.getTicketHistory() != null) {
//                         for (final Ticket.Action action : ticket.getTicketHistory().getActions()) {
//                             if (username.equals(action.getBy())) {
//                                 shouldAddTicket = true;
//                                 break;
//                             }
//                         }
//                     }
//
//                     if (!shouldAddTicket && ticket.getComments() != null) {
//                         for (final Ticket.Comment comment : ticket.getComments()) {
//                             if (username.equals(comment.getAuthor())) {
//                                 shouldAddTicket = true;
//                                 break;
//                             }
//                         }
//                     }
//                     break;
//
//                 case "MANAGER":
//                     final List<Milestone> managerMilestones = milestones.stream()
//                             .filter(milestone -> milestone.getOwner().equals(username))
//                             .collect(Collectors.toList());
//
//                     for (final Milestone milestone : managerMilestones) {
//                         if (milestone.containsTicket(ticket.getId())) {
//                             shouldAddTicket = true;
//                             break;
//                         }
//                     }
//                     break;
//             }
//
//             if (shouldAddTicket) {
//                 filteredTickets.add(ticket);
//             }
//         }
//
//         Collections.sort(filteredTickets, (t1, t2) -> {
//             final int dateCompare = t1.getCreatedAt().compareTo(t2.getCreatedAt());
//             if (dateCompare != 0)
//                 return dateCompare;
//             return Integer.compare(t1.getId(), t2.getId());
//         });
//
//         return filteredTickets;
//     }
//
//     // ==================== METODE DE OPERAȚIUNE ====================
//
//     public void assignTicketToDeveloper(final Ticket ticket, final CommandInput command) {
//         ticket.assignDeveloper(command);
//     }
//
//     public void undoAssignTicket(final Ticket ticket, final CommandInput command) {
//         ticket.undoAssignDeveloper(command);
//     }
//
//     public void addCommentToTicket(final Ticket ticket, final CommandInput command) {
//         ticket.addComment(command.username(), command.comment(), command.timestamp());
//     }
//
//     public void undoAddComment(final Ticket ticket, final CommandInput command) {
//         ticket.undoAddComment(command.username());
//     }
//
//     public void changeTicketStatus(final Ticket ticket, final Status newStatus,
//             final CommandInput command) {
//         ticket.changeStatus(newStatus, command.username(), command.timestamp());
//     }
//
//     public TicketHistory getTicketHistory(final int id) {
//         final Ticket ticket = getTicket(id);
//         return ticket != null ? ticket.getTicketHistory() : null;
//     }
//
//     // ==================== METODE DE CĂUTARE/FILTRARE ====================
//
//     public List<Ticket> filterTickets(final User user,
//             final FiltersInput filters,
//             final List<Milestone> milestones) {
//         final List<Ticket> accessibleTickets = new ArrayList<>();
//
//         if ("MANAGER".equals(user.getRole().name())) {
//             accessibleTickets.addAll(tickets);
//         } else if ("DEVELOPER".equals(user.getRole().name())) {
//             final Developer dev = (Developer) user;
//             final List<Milestone> devMilestones = milestones.stream()
//                     .filter(milestone -> milestone.getAssignedDevs() != null &&
//                             Arrays.stream(milestone.getAssignedDevs())
//                                     .anyMatch(developer -> developer.equals(dev.getUsername())))
//                     .collect(Collectors.toList());
//
//             for (final Milestone milestone : devMilestones) {
//                 for (final int ticketId : milestone.getTickets()) {
//                     for (final Ticket ticket : tickets) {
//                         if (ticket.getId() == ticketId &&
//                                 ticket.getStatus() == Ticket.Status.OPEN) {
//
//                             boolean alreadyAdded = false;
//                             for (final Ticket addedTicket : accessibleTickets) {
//                                 if (addedTicket.getId() == ticketId) {
//                                     alreadyAdded = true;
//                                     break;
//                                 }
//                             }
//
//                             if (!alreadyAdded) {
//                                 accessibleTickets.add(ticket);
//                             }
//                             break;
//                         }
//                     }
//                 }
//             }
//         } else {
//             return new ArrayList<>();
//         }
//
//         final FilterContext<Ticket> context = new FilterContext<>();
//
//         context.addStrategy("businessPriority", new BusinessPriorityFilter());
//         context.addStrategy("type", new TypeFilter());
//         context.addStrategy("createdAt", new CreatedAtFilter());
//         context.addStrategy("createdBefore", new CreatedBeforeFilter());
//         context.addStrategy("createdAfter", new CreatedAfterFilter());
//         context.addStrategy("keywords", new KeywordsFilter());
//
//         if ("DEVELOPER".equals(user.getRole().name())) {
//             final Developer dev = (Developer) user;
//             context.addStrategy("availableForAssignment", new AvailableForAssignmentFilter(dev));
//         }
//
//         final Map<String, String> filterMap = convertFiltersToMap(filters);
//         final List<Ticket> filtered = context.applyFilters(accessibleTickets, filterMap);
//
//         filtered.sort(Comparator
//                 .comparing(Ticket::getCreatedAt)
//                 .thenComparing(Ticket::getId));
//
//         return filtered;
//     }
//
//     private Map<String, String> convertFiltersToMap(final FiltersInput filters) {
//         final Map<String, String> map = new HashMap<>();
//
//         if (filters.businessPriority() != null) {
//             map.put("businessPriority", filters.businessPriority());
//         }
//         if (filters.type() != null) {
//             map.put("type", filters.type());
//         }
//         if (filters.createdAt() != null) {
//             map.put("createdAt", filters.createdAt());
//         }
//         if (filters.createdBefore() != null) {
//             map.put("createdBefore", filters.createdBefore());
//         }
//         if (filters.createdAfter() != null) {
//             map.put("createdAfter", filters.createdAfter());
//         }
//         if (filters.availableForAssignment() != null) {
//             map.put("availableForAssignment", String.valueOf(filters.availableForAssignment()));
//         }
//         if (filters.keywords() != null && filters.keywords().length > 0) {
//             map.put("keywords", Arrays.toString(filters.keywords()));
//         }
//
//         return map;
//     }
//
//     // ==================== METODE DE UPDATE ====================
//
//     public void update(final LocalDate date) {
//         // Aici poți adăuga logica de update pentru tichete dacă e necesar
//         // De exemplu, upgrade la prioritate după un număr de zile
//     }
//
//     // ==================== METODE HELPER ====================
//
//     public List<Ticket> getTicketsByStatus(final Ticket.Status status) {
//         return tickets.stream()
//                 .filter(ticket -> ticket.getStatus() == status)
//                 .collect(Collectors.toList());
//     }
//
//     public List<Ticket> getTicketsByType(final String type) {
//         return tickets.stream()
//                 .filter(ticket -> ticket.getType().equals(type))
//                 .collect(Collectors.toList());
//     }
//
//     public long countTicketsByStatus(final Ticket.Status status) {
//         return tickets.stream()
//                 .filter(ticket -> ticket.getStatus() == status)
//                 .count();
//     }
// }
