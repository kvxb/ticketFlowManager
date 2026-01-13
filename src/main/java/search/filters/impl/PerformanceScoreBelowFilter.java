package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

public class PerformanceScoreBelowFilter implements DeveloperFilterStrategy {
    
    @Override
    public List<Developer> filter(List<Developer> developers, String filterValue) {
        List<Developer> filteredDevelopers = new ArrayList<>();
        double maxScore = Double.parseDouble(filterValue);
        
        for (Developer developer : developers) {
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
