package search.filters.impl;

import search.filters.TicketFilterStrategy;
import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;

public class KeywordsFilter implements TicketFilterStrategy {

    @Override
    public List<Ticket> filter(List<Ticket> tickets, String filterValue) {
        List<Ticket> filteredTickets = new ArrayList<>();
        String[] keywords = parseKeywords(filterValue);

        for (Ticket ticket : tickets) {
            List<String> matchingWords = findMatchingWords(ticket, keywords);
            if (!matchingWords.isEmpty()) {
                ticket.setMatchingWords(matchingWords);
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private String[] parseKeywords(String jsonArray) {
        if (jsonArray == null || jsonArray.equals("null") || jsonArray.isEmpty()) {
            return new String[0];
        }

        String cleaned = jsonArray.replace("[", "").replace("]", "")
                .replace("\"", "").replace(" ", "");
        if (cleaned.isEmpty()) {
            return new String[0];
        }
        return cleaned.split(",");
    }

    private List<String> findMatchingWords(Ticket ticket, String[] keywords) {
        List<String> matchingWords = new ArrayList<>();

        String title = ticket.getTitle();
        String description = ticket.getDescription();

        if (title == null && description == null) {
            return matchingWords;
        }

        String titleLower = title != null ? title.toLowerCase() : "";
        String descLower = description != null ? description.toLowerCase() : "";

        for (String keyword : keywords) {
            String keywordLower = keyword.toLowerCase();
            if (titleLower.contains(keywordLower) || descLower.contains(keywordLower)) {
                matchingWords.add(keyword);
            }
        }
        return matchingWords;
    }

    @Override
    public String getFilterName() {
        return "keywords";
    }
}
