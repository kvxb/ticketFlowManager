package search.filters;

import tickets.Ticket;

/**
 * Strategy for Ticket search
 */
public interface TicketFilterStrategy extends FilterStrategy<Ticket> {
    /**
     * Get filter name
     */
    String getFilterName();
}
