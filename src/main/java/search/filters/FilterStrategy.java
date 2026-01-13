package search.filters;

import java.util.List;

public interface FilterStrategy<T> {
    List<T> filter(List<T> items, String filterValue);
}
