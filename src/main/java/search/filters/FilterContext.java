package search.filters;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Context for the search command
 */
public final class FilterContext<T> {
    private final Map<String, FilterStrategy<T>> strategies;

    public FilterContext() {
        this.strategies = new HashMap<>();
    }

    /**
     * Adds a strategy to the filter context
     */
    public void addStrategy(final String filterName, final FilterStrategy<T> strategy) {
        strategies.put(filterName, strategy);
    }

    /**
     * Apply the filters and return the tickets/devs that match the needs
     */
    public List<T> applyFilters(final List<T> items, final Map<String, String> filters) {
        List<T> result = new ArrayList<>(items);

        for (final Map.Entry<String, String> filterEntry : filters.entrySet()) {
            final String filterName = filterEntry.getKey();
            final String filterValue = filterEntry.getValue();

            final FilterStrategy<T> strategy = strategies.get(filterName);
            if (strategy != null && filterValue != null && !filterValue.trim().isEmpty()) {
                result = strategy.filter(result, filterValue);
            }
        }

        return result;
    }
}
