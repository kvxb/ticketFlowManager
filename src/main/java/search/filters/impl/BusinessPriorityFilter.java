package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;

public class BusinessPriorityFilter implements TicketFilterStrategy {
    
    @Override
    public List<Ticket> filter(List<Ticket> tickets, String filterValue) {
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getBusinessPriority().toString().equalsIgnoreCase(filterValue)) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }
    
    @Override
    public String getFilterName() {
        return "businessPriority";
    }
}
