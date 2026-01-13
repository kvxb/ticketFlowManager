// package search;
//
// import search.filters.FilterContext;
// import search.filters.TicketFilterStrategy;
// import search.filters.DeveloperFilterStrategy;
// import search.filters.impl.*;
// import tickets.Ticket;
// import users.User;
// import users.Developer;
// import users.Manager;
// import java.util.List;
// import java.util.Map;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Comparator;
//
// public class SearchService {
//
//     public List<Ticket> searchTickets(User user, List<Ticket> allTickets, 
//                                      Map<String, String> filters) {
//         List<Ticket> accessibleTickets = filterByRole(user, allTickets);
//
//         FilterContext<Ticket> context = createTicketFilterContext(user);
//         List<Ticket> filteredTickets = context.applyFilters(accessibleTickets, filters);
//
//         sortTickets(filteredTickets);
//         return filteredTickets;
//     }
//
//     public List<Developer> searchDevelopers(Manager manager, List<Developer> allDevelopers,
//                                            Map<String, String> filters) {
//         List<Developer> subordinates = manager.getSubordinates();
//
//         FilterContext<Developer> context = createDeveloperFilterContext();
//         List<Developer> filteredDevelopers = context.applyFilters(subordinates, filters);
//
//         sortDevelopers(filteredDevelopers);
//         return filteredDevelopers;
//     }
//
//     private List<Ticket> filterByRole(User user, List<Ticket> allTickets) {
//         List<Ticket> filtered = new ArrayList<>();
//
//         if (user instanceof Developer) {
//             Developer dev = (Developer) user;
//             for (Ticket ticket : allTickets) {
//                 if (ticket.getStatus() == Ticket.Status.OPEN &&
//                     dev.getAssignedMilestones().contains(ticket.getMilestone())) {
//                     filtered.add(ticket);
//                 }
//             }
//         } else if (user instanceof Manager) {
//             filtered.addAll(allTickets);
//         }
//
//         return filtered;
//     }
//
//     private FilterContext<Ticket> createTicketFilterContext(User user) {
//         FilterContext<Ticket> context = new FilterContext<>();
//
//         context.addStrategy("businessPriority", new BusinessPriorityFilter());
//         context.addStrategy("type", new TypeFilter());
//         context.addStrategy("createdAt", new CreatedAtFilter());
//         context.addStrategy("createdBefore", new CreatedBeforeFilter());
//         context.addStrategy("createdAfter", new CreatedAfterFilter());
//
//         if (user instanceof Developer) {
//             context.addStrategy("availableForAssignment", 
//                               new AvailableForAssignmentFilter((Developer) user));
//         }
//
//         if (user instanceof Manager) {
//             context.addStrategy("keywords", new KeywordsFilter());
//         }
//
//         return context;
//     }
//
//     private FilterContext<Developer> createDeveloperFilterContext() {
//         FilterContext<Developer> context = new FilterContext<>();
//
//         context.addStrategy("expertiseArea", new ExpertiseAreaFilter());
//         context.addStrategy("seniority", new SeniorityFilter());
//         context.addStrategy("performanceScoreAbove", new PerformanceScoreAboveFilter());
//         context.addStrategy("performanceScoreBelow", new PerformanceScoreBelowFilter());
//
//         return context;
//     }
//
//     private void sortTickets(List<Ticket> tickets) {
//         Collections.sort(tickets, Comparator
//             .comparing(Ticket::getCreatedAt)
//             .thenComparing(Ticket::getId));
//     }
//
//     private void sortDevelopers(List<Developer> developers) {
//         Collections.sort(developers, Comparator
//             .comparing(Developer::getUsername));
//     }
// }
