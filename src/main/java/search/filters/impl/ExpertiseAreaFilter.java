package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

/**
 * Filters developers based on their expertise area.
 */
public final class ExpertiseAreaFilter implements DeveloperFilterStrategy {

    @Override
    public List<Developer> filter(final List<Developer> developers, final String filterValue) {
        final List<Developer> filteredDevelopers = new ArrayList<>();

        for (final Developer developer : developers) {
            if (developer.getExpertiseArea().name().equalsIgnoreCase(filterValue)) {
                filteredDevelopers.add(developer);
            }
        }
        return filteredDevelopers;
    }

    @Override
    public String getFilterName() {
        return "expertiseArea";
    }
}
