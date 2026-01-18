package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

/**
 * Filters developers with a performance score below a certain threshold.
 */
public final class PerformanceScoreBelowFilter implements DeveloperFilterStrategy {

    @Override
    public List<Developer> filter(final List<Developer> developers, final String filterValue) {
        final List<Developer> filteredDevelopers = new ArrayList<>();
        final double maxScore = Double.parseDouble(filterValue);

        for (final Developer developer : developers) {
            if (developer.getPerformanceScore() <= maxScore) {
                filteredDevelopers.add(developer);
            }
        }
        return filteredDevelopers;
    }

    @Override
    public String getFilterName() {
        return "performanceScoreBelow";
    }
}
