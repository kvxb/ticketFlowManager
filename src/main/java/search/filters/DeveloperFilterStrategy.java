package search.filters;

import users.Developer;
import java.util.List;

public interface DeveloperFilterStrategy extends FilterStrategy<Developer> {
    String getFilterName();
}
