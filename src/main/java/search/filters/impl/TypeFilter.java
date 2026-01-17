package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;

public class TypeFilter implements TicketFilterStrategy {

    @Override
    public List<Ticket> filter(final List<Ticket> tickets, final String filterValue) {
        final List<Ticket> filteredTickets = new ArrayList<>();
        for (final Ticket ticket : tickets) {
            if (ticket.getType().toString().equalsIgnoreCase(filterValue)) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    @Override
    public String getFilterName() {
        return "type";
    }
}
