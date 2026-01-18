package search.filters;

import java.util.List;

/**
 * Strategy for filters used by the search command
 */
public interface FilterStrategy<T> {
    /**
     * Filter for the set of possible devs/tickets
     */
    List<T> filter(List<T> items, String filterValue);
}
