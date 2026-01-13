package search.filters;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class FilterContext<T> {
    private Map<String, FilterStrategy<T>> strategies;

    public FilterContext() {
        this.strategies = new HashMap<>();
    }

    public void addStrategy(String filterName, FilterStrategy<T> strategy) {
        strategies.put(filterName, strategy);
    }

    public List<T> applyFilters(List<T> items, Map<String, String> filters) {
        List<T> result = new ArrayList<>(items);

        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
            String filterName = filterEntry.getKey();
            String filterValue = filterEntry.getValue();

            FilterStrategy<T> strategy = strategies.get(filterName);
            if (strategy != null && filterValue != null && !filterValue.trim().isEmpty()) {
                result = strategy.filter(result, filterValue);
            }
        }

        return result;
    }
}
