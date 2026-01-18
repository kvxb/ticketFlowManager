package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

/**
 * Filters tickets based on whether they can be assigned to the current developer.
 */
public final class AvailableForAssignmentFilter implements TicketFilterStrategy {

    private final Developer currentDeveloper;

    /**
     * Constructor.
     * @param currentDeveloper The developer checking for assignable tickets.
     */
    public AvailableForAssignmentFilter(final Developer currentDeveloper) {
        this.currentDeveloper = currentDeveloper;
    }

    @Override
    public List<Ticket> filter(final List<Ticket> tickets, final String filterValue) {
        if (!Boolean.parseBoolean(filterValue)) {
            return tickets;
        }

        final List<Ticket> filteredTickets = new ArrayList<>();
        for (final Ticket ticket : tickets) {
            if (canBeAssigned(ticket)) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private boolean canBeAssigned(final Ticket ticket) {
        if (currentDeveloper == null) {
            return false;
        }
        if (ticket.getStatus() != Ticket.Status.OPEN) {
            return false;
        }

        return currentDeveloper.canHandleTicket(ticket);
    }

    @Override
    public String getFilterName() {
        return "availableForAssignment";
    }
}
