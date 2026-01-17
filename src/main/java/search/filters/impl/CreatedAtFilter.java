package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class CreatedAtFilter implements TicketFilterStrategy {

    @Override
    public List<Ticket> filter(final List<Ticket> tickets, final String filterValue) {
        final List<Ticket> filteredTickets = new ArrayList<>();
        final LocalDate targetDate = LocalDate.parse(filterValue);

        for (final Ticket ticket : tickets) {
            if (ticket.getCreatedAt().equals(targetDate.toString())) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    @Override
    public String getFilterName() {
        return "createdAt";
    }
}
