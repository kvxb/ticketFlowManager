package services;

import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;
import mathutils.MathUtil;

import database.Database;

public class AnalyticsService {
    private static final Database db = Database.getInstance();

    public static List<Number> getCustomerImpact(List<Ticket> tickets) {
        final List<Number> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getImpact();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        report.add(MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        report.add(MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        report.add(MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        return report;
    }

    public static List<Number> getResolutionEfficiency(List<Ticket> tickets) {
        final List<Number> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : tickets) {
            if (ticket.getStatus().name().equals("OPEN")) {
                continue;
            }
            if (ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getEfficiency();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        report.add(MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        report.add(MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        report.add(MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        return report;
    }

    public List<Object> getAppStability() {
        final List<Object> report = new ArrayList<>();

        int totalOpenTickets = 0;
        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        double bugRiskScore = 0.0;
        double featureRequestRiskScore = 0.0;
        double uiFeedbackRiskScore = 0.0;

        for (final Ticket ticket : db.getTickets()) {
            if (!ticket.getStatus().name().equals("OPEN") &&
                    !ticket.getStatus().name().equals("IN_PROGRESS")) {
                continue;
            }

            totalOpenTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += ticket.getImpact();
                    bugRiskScore += ticket.getRisk();
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += ticket.getImpact();
                    featureRequestRiskScore += ticket.getRisk();
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += ticket.getImpact();
                    uiFeedbackRiskScore += ticket.getRisk();
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }

        report.add(totalOpenTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        final double avgBugRisk = bugCount > 0 ? bugRiskScore / bugCount : 0;
        final double avgFeatureRisk = featureRequestCount > 0 ? featureRequestRiskScore / featureRequestCount : 0;
        final double avgUIRisk = uiFeedbackCount > 0 ? uiFeedbackRiskScore / uiFeedbackCount : 0;

        final String bugRiskLevel = getRiskLevel(avgBugRisk);
        final String featureRiskLevel = getRiskLevel(avgFeatureRisk);
        final String uiRiskLevel = getRiskLevel(avgUIRisk);

        report.add(bugRiskLevel);
        report.add(featureRiskLevel);
        report.add(uiRiskLevel);

        final double avgBugImpact = bugCount > 0 ? bugImpact / bugCount : 0;
        final double avgFeatureImpact = featureRequestCount > 0 ? featureRequestImpact / featureRequestCount : 0;
        final double avgUIImpact = uiFeedbackCount > 0 ? uiFeedbackImpact / uiFeedbackCount : 0;

        report.add(MathUtil.round(avgBugImpact));
        report.add(MathUtil.round(avgFeatureImpact));
        report.add(MathUtil.round(avgUIImpact));

        final String appStability = determineStability(bugRiskLevel, featureRiskLevel, uiRiskLevel,
                avgBugImpact, avgFeatureImpact, avgUIImpact);
        report.add(appStability);

        return report;
    }

    private String getRiskLevel(final double impact) {
        if (impact >= 0 && impact <= 24)
            return "NEGLIGIBLE";
        if (impact >= 25 && impact <= 49)
            return "MODERATE";
        if (impact >= 50 && impact <= 74)
            return "SIGNIFICANT";
        if (impact >= 75 && impact <= 100)
            return "MAJOR";
        return "NEGLIGIBLE";
    }

    private String determineStability(final String bugRisk, final String featureRisk, final String uiRisk,
            final double bugImpact, final double featureImpact, final double uiImpact) {

        final boolean hasSignificantRisk = bugRisk.equals("SIGNIFICANT") ||
                bugRisk.equals("MAJOR") ||
                featureRisk.equals("SIGNIFICANT") ||
                featureRisk.equals("MAJOR") ||
                uiRisk.equals("SIGNIFICANT") ||
                uiRisk.equals("MAJOR");

        final boolean allNegligible = bugRisk.equals("NEGLIGIBLE") &&
                featureRisk.equals("NEGLIGIBLE") &&
                uiRisk.equals("NEGLIGIBLE");

        final boolean allImpactBelow50 = bugImpact < 50 && featureImpact < 50 && uiImpact < 50;

        if (hasSignificantRisk) {
            return "UNSTABLE";
        }

        if (allNegligible && allImpactBelow50) {
            return "STABLE";
        }

        return "PARTIALLY STABLE";
    }

    public List<Object> getTicketRisk() {
        final List<Object> report = new ArrayList<>();

        int totalTickets = 0;

        int bugCount = 0;
        int featureRequestCount = 0;
        int uiFeedbackCount = 0;

        int lowPriority = 0;
        int mediumPriority = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        double bugImpact = 0.0;
        double featureRequestImpact = 0.0;
        double uiFeedbackImpact = 0.0;

        for (final Ticket ticket : db.getTickets()) {
            if (ticket.getStatus().name().equals("RESOLVED")) {
                continue;
            }
            if (ticket.getStatus().name().equals("CLOSED")) {
                continue;
            }
            totalTickets++;
            final String type = ticket.getType();
            final String priority = ticket.getBusinessPriority().name();
            final double impact = ticket.getRisk();

            switch (type) {
                case "BUG":
                    bugCount++;
                    bugImpact += impact;
                    break;
                case "FEATURE_REQUEST":
                    featureRequestCount++;
                    featureRequestImpact += impact;
                    break;
                case "UI_FEEDBACK":
                    uiFeedbackCount++;
                    uiFeedbackImpact += impact;
                    break;
            }

            switch (priority) {
                case "LOW":
                    lowPriority++;
                    break;
                case "MEDIUM":
                    mediumPriority++;
                    break;
                case "HIGH":
                    highPriority++;
                    break;
                case "CRITICAL":
                    criticalPriority++;
                    break;
            }
        }
        report.add(totalTickets);

        report.add(bugCount);
        report.add(featureRequestCount);
        report.add(uiFeedbackCount);

        report.add(lowPriority);
        report.add(mediumPriority);
        report.add(highPriority);
        report.add(criticalPriority);

        bugImpact = (MathUtil.round(MathUtil.average(bugImpact, bugCount)));
        featureRequestImpact = (MathUtil.round(MathUtil.average(featureRequestImpact, featureRequestCount)));
        uiFeedbackImpact = (MathUtil.round(MathUtil.average(uiFeedbackImpact, uiFeedbackCount)));

        final String bugRiskLevel = getRiskLevel(bugImpact);
        final String featureRequestRiskLevel = getRiskLevel(featureRequestImpact);
        final String uiFeedbackRiskLevel = getRiskLevel(uiFeedbackImpact);

        report.add(bugRiskLevel);
        report.add(featureRequestRiskLevel);
        report.add(uiFeedbackRiskLevel);

        return report;
    }
}
