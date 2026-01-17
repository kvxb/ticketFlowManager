package search;

import search.filters.FilterContext;
import search.filters.impl.*;
import tickets.Ticket;
import milestones.Milestone;
import users.Developer;
import users.Manager;
import users.User;
import io.FiltersInput;
import java.util.*;
import database.Database;

public class SearchService {

    public static List<?> getSearchResults(io.CommandInput command) {
        Database db = Database.getInstance();
        User user = db.getUser(command.username());
        FiltersInput filters = command.filters();
        String searchType = filters.searchType();

        if ("DEVELOPER".equals(searchType)) {
            if (!"MANAGER".equals(user.getRole().name())) {
                return new ArrayList<>();
            }
            return filterDevelopers((Manager) user, db.getAllDevelopers(), filters);
        } else {
            return filterTickets(user, db.getAllTickets(), filters);
        }
    }

    private static List<Ticket> filterTickets(final User user, final List<Ticket> allTickets,
            final FiltersInput filters) {
        final List<Ticket> accessibleTickets = new ArrayList<>();
        Database db = Database.getInstance();

        if ("MANAGER".equals(user.getRole().name())) {
            accessibleTickets.addAll(allTickets);
        } else if ("DEVELOPER".equals(user.getRole().name())) {
            final Developer dev = (Developer) user;
            final List<Milestone> allMilestones = db.getMilestones();

            for (final Milestone milestone : allMilestones) {
                // Check if developer is assigned to this milestone
                boolean isAssigned = false;
                if (milestone.getAssignedDevs() != null) {
                    for (String assignee : milestone.getAssignedDevs()) {
                        if (assignee.equals(dev.getUsername())) {
                            isAssigned = true;
                            break;
                        }
                    }
                }

                if (isAssigned) {
                    for (final int ticketId : milestone.getTickets()) {
                        for (final Ticket ticket : allTickets) {
                            if (ticket.getId() == ticketId &&
                                    ticket.getStatus() == Ticket.Status.OPEN) {

                                boolean alreadyAdded = false;
                                for (final Ticket addedTicket : accessibleTickets) {
                                    if (addedTicket.getId() == ticketId) {
                                        alreadyAdded = true;
                                        break;
                                    }
                                }

                                if (!alreadyAdded) {
                                    accessibleTickets.add(ticket);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            return new ArrayList<>();
        }

        final FilterContext<Ticket> context = new FilterContext<>();

        context.addStrategy("businessPriority", new BusinessPriorityFilter());
        context.addStrategy("type", new TypeFilter());
        context.addStrategy("createdAt", new CreatedAtFilter());
        context.addStrategy("createdBefore", new CreatedBeforeFilter());
        context.addStrategy("createdAfter", new CreatedAfterFilter());
        context.addStrategy("keywords", new KeywordsFilter());

        if ("DEVELOPER".equals(user.getRole().name())) {
            final Developer dev = (Developer) user;
            context.addStrategy("availableForAssignment", new AvailableForAssignmentFilter(dev));
        }

        final Map<String, String> filterMap = convertToMap(filters);
        final List<Ticket> filtered = context.applyFilters(accessibleTickets, filterMap);

        filtered.sort(Comparator
                .comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        return filtered;
    }

    private static List<Developer> filterDevelopers(final Manager manager, final List<Developer> allDevelopers,
            final FiltersInput filters) {
        final List<Developer> subordinates = new ArrayList<>();
        String[] subArr = manager.getSubordinates();

        if (subArr != null) {
            final List<String> subordinateUsernames = Arrays.asList(subArr);
            for (final Developer dev : allDevelopers) {
                if (subordinateUsernames.contains(dev.getUsername())) {
                    subordinates.add(dev);
                }
            }
        }

        final FilterContext<Developer> context = new FilterContext<>();

        context.addStrategy("expertiseArea", new ExpertiseAreaFilter());
        context.addStrategy("seniority", new SeniorityFilter());
        context.addStrategy("performanceScoreAbove", new PerformanceScoreAboveFilter());
        context.addStrategy("performanceScoreBelow", new PerformanceScoreBelowFilter());

        final Map<String, String> filterMap = convertToMap(filters);
        final List<Developer> filtered = context.applyFilters(subordinates, filterMap);

        filtered.sort(Comparator.comparing(Developer::getUsername));

        return filtered;
    }

    private static Map<String, String> convertToMap(final FiltersInput filters) {
        final Map<String, String> map = new HashMap<>();

        if (filters.businessPriority() != null) {
            map.put("businessPriority", filters.businessPriority());
        }
        if (filters.type() != null) {
            map.put("type", filters.type());
        }
        if (filters.createdAt() != null) {
            map.put("createdAt", filters.createdAt());
        }
        if (filters.createdBefore() != null) {
            map.put("createdBefore", filters.createdBefore());
        }
        if (filters.createdAfter() != null) {
            map.put("createdAfter", filters.createdAfter());
        }
        if (filters.availableForAssignment() != null) {
            map.put("availableForAssignment", String.valueOf(filters.availableForAssignment()));
        }
        if (filters.keywords() != null && filters.keywords().length > 0) {
            map.put("keywords", Arrays.toString(filters.keywords()));
        }
        if (filters.expertiseArea() != null) {
            map.put("expertiseArea", filters.expertiseArea());
        }
        if (filters.seniority() != null) {
            map.put("seniority", filters.seniority());
        }
        if (filters.performanceScoreAbove() > 0) {
            map.put("performanceScoreAbove", String.valueOf(filters.performanceScoreAbove()));
        }
        if (filters.performanceScoreBelow() > 0) {
            map.put("performanceScoreBelow", String.valueOf(filters.performanceScoreBelow()));
        }

        return map;
    }
}
