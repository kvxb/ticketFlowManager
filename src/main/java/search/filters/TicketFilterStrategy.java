package search.filters;

import tickets.Ticket;
import java.util.List;

public interface TicketFilterStrategy extends FilterStrategy<Ticket> {
    String getFilterName();
}
