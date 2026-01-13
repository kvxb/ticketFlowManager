package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

public class PerformanceScoreAboveFilter implements DeveloperFilterStrategy {
    
    @Override
    public List<Developer> filter(List<Developer> developers, String filterValue) {
        List<Developer> filteredDevelopers = new ArrayList<>();
        double minScore = Double.parseDouble(filterValue);
        
        for (Developer developer : developers) {
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
