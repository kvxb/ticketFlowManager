package io;

public record FiltersInput (
    String businessPriority,
    int performanceScoreAbove,
    int performanceScoreBelow,
    String type,
    String createdAt,
    String createdBefore,
    String createdAfter,
    Boolean availableForAssignment,
    String searchType,
    String expertiseArea,
    String[] keywords,
    String seniority
) {

}
