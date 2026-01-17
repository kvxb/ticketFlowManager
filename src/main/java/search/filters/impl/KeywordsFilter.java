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
    public List<Ticket> filter(final List<Ticket> tickets, final String filterValue) {
        final List<Ticket> filteredTickets = new ArrayList<>();
        final String[] keywords = parseKeywords(filterValue);

        for (final Ticket ticket : tickets) {
            final List<String> matchingWords = findMatchingWords(ticket, keywords);
            if (!matchingWords.isEmpty()) {
                ticket.setMatchingWords(matchingWords);
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private String[] parseKeywords(final String jsonArray) {
        if (jsonArray == null || jsonArray.equals("null") || jsonArray.isEmpty()) {
            return new String[0];
        }

        final String cleaned = jsonArray.replace("[", "").replace("]", "")
                .replace("\"", "").replace(" ", "");
        if (cleaned.isEmpty()) {
            return new String[0];
        }
        return cleaned.split(",");
    }

    private List<String> findMatchingWords(final Ticket ticket, final String[] keywords) {
        final List<String> matchingWords = new ArrayList<>();

        final String title = ticket.getTitle();
        final String description = ticket.getDescription();

        if (title == null && description == null) {
            return matchingWords;
        }

        final String titleLower = title != null ? title.toLowerCase() : "";
        final String descLower = description != null ? description.toLowerCase() : "";

        for (final String keyword : keywords) {
            final String keywordLower = keyword.toLowerCase();
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
