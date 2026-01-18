package search.filters;

import users.Developer;

/**
 * Strategy for Developer search
 */
public interface DeveloperFilterStrategy extends FilterStrategy<Developer> {
    /**
     * Get filter name
     */
    String getFilterName();
}
