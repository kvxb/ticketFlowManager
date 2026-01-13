package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

public class AvailableForAssignmentFilter implements TicketFilterStrategy {

    private Developer currentDeveloper;

    public AvailableForAssignmentFilter(Developer currentDeveloper) {
        this.currentDeveloper = currentDeveloper;
    }

    @Override
    public List<Ticket> filter(List<Ticket> tickets, String filterValue) {
        if (!Boolean.parseBoolean(filterValue)) {
            return tickets;
        }

        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (canBeAssigned(ticket)) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private boolean canBeAssigned(Ticket ticket) {
        if (currentDeveloper == null)
            return false;
        if (ticket.getStatus() != Ticket.Status.OPEN)
            return false;

        return currentDeveloper.canHandleTicket(ticket);
    }

    @Override
    public String getFilterName() {
        return "availableForAssignment";
    }
}
