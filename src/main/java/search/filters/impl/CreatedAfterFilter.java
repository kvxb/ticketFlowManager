package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class CreatedAfterFilter implements TicketFilterStrategy {
    
    @Override
    public List<Ticket> filter(List<Ticket> tickets, String filterValue) {
        List<Ticket> filteredTickets = new ArrayList<>();
        LocalDate targetDate = LocalDate.parse(filterValue);
        
        for (Ticket ticket : tickets) {
            if (LocalDate.parse(ticket.getCreatedAt()).isAfter(targetDate)){
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }
    
    @Override
    public String getFilterName() {
        return "createdAfter";
    }
}
