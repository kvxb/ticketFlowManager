package io;

/**
 * Holds the param fields
 */
public record ParamsInput(
        String type,
        String title,
        String businessPriority,
        String reportedBy,
        String expertiseArea,
        String description,
        String expectedBehavior,
        String actualBehavior,
        String frequency,
        String severity,
        String environment,
        String errorCode,
        String uiElementId,
        String businessValue,
        String screenshotUrl,
        int usabilityScore,
        String suggestedFix,
        String customerDemand) {

}
