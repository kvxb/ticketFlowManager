package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

public class PerformanceScoreAboveFilter implements DeveloperFilterStrategy {

    @Override
    public List<Developer> filter(final List<Developer> developers, final String filterValue) {
        final List<Developer> filteredDevelopers = new ArrayList<>();
        final double minScore = Double.parseDouble(filterValue);

        for (final Developer developer : developers) {
            if (developer.getPerformanceScore() >= minScore) {
                filteredDevelopers.add(developer);
            }
        }
        return filteredDevelopers;
    }

    @Override
    public String getFilterName() {
        return "performanceScoreAbove";
    }
}
