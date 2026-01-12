package search.filters;

import tickets.Ticket;
import users.User;

public interface FilterStrategy {
    boolean apply(Ticket ticket, User currentUser);
    String getFilterType();
}
