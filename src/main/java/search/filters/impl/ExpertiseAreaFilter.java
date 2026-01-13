package search.filters.impl;

import search.filters.DeveloperFilterStrategy;
import users.Developer;
import java.util.List;
import java.util.ArrayList;

public class ExpertiseAreaFilter implements DeveloperFilterStrategy {
    
    @Override
    public List<Developer> filter(List<Developer> developers, String filterValue) {
        List<Developer> filteredDevelopers = new ArrayList<>();
        
        for (Developer developer : developers) {
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
