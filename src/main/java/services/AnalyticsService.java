package services;

import tickets.Ticket;
import java.util.List;
import java.util.ArrayList;
import mathutils.MathUtil;

import database.Database;

/**
 * Service class for calculating analytics and statistics.
 */
public final class AnalyticsService {
    private static final Database DB = Database.getInstance();

    private static final int RISK_NEGLIGIBLE_MAX = 24;
    private static final int RISK_MODERATE_MIN = 25;
    private static final int RISK_MODERATE_MAX = 49;
    private static final int RISK_SIGNIFICANT_MIN = 50;
    private static final int RISK_SIGNIFICANT_MAX = 74;
    private static final int RISK_MAJOR_MIN = 75;
    private static final int RISK_MAJOR_MAX = 100;
    private static final int IMPACT_THRESHOLD = 50;

    /**
     * Default constructor.
     */
    public AnalyticsService() {
    }

    /**
     * Calculates customer impact metrics based on a list of tickets.
     *
     * @param tickets The list of tickets to analyze.
     * @return A list containing impact statistics.
     */
    public static List<Number> getCustomerImpact(final List<Ticket> tickets) {
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
                default:
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
                default:
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

    /**
     * Calculates resolution efficiency metrics based on a list of tickets.
     *
     * @param tickets The list of tickets to analyze.
     * @return A list containing efficiency statistics.
     */
    public static List<Number> getResolutionEfficiency(final List<Ticket> tickets) {
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
                default:
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
                default:
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

    /**
     * Calculates application stability metrics based on open and in-progress
     * tickets.
     *
     * @return A list containing stability statistics.
     */
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

        for (final Ticket ticket : DB.getTickets()) {
            if (!ticket.getStatus().name().equals("OPEN")
                    && !ticket.getStatus().name().equals("IN_PROGRESS")) {
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
                default:
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
                default:
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
        final double avgFeatureRisk = featureRequestCount > 0
                ? featureRequestRiskScore / featureRequestCount
                : 0;
        final double avgUIRisk = uiFeedbackCount > 0
                ? uiFeedbackRiskScore / uiFeedbackCount
                : 0;

        final String bugRiskLevel = getRiskLevel(avgBugRisk);
        final String featureRiskLevel = getRiskLevel(avgFeatureRisk);
        final String uiRiskLevel = getRiskLevel(avgUIRisk);

        report.add(bugRiskLevel);
        report.add(featureRiskLevel);
        report.add(uiRiskLevel);

        final double avgBugImpact = bugCount > 0 ? bugImpact / bugCount : 0;
        final double avgFeatureImpact = featureRequestCount > 0
                ? featureRequestImpact / featureRequestCount
                : 0;
        final double avgUIImpact = uiFeedbackCount > 0
                ? uiFeedbackImpact / uiFeedbackCount
                : 0;

        report.add(MathUtil.round(avgBugImpact));
        report.add(MathUtil.round(avgFeatureImpact));
        report.add(MathUtil.round(avgUIImpact));

        final String appStability = determineStability(bugRiskLevel, featureRiskLevel, uiRiskLevel,
                avgBugImpact, avgFeatureImpact, avgUIImpact);
        report.add(appStability);

        return report;
    }

    private String getRiskLevel(final double impact) {
        if (impact >= 0 && impact <= RISK_NEGLIGIBLE_MAX) {
            return "NEGLIGIBLE";
        }
        if (impact >= RISK_MODERATE_MIN && impact <= RISK_MODERATE_MAX) {
            return "MODERATE";
        }
        if (impact >= RISK_SIGNIFICANT_MIN && impact <= RISK_SIGNIFICANT_MAX) {
            return "SIGNIFICANT";
        }
        if (impact >= RISK_MAJOR_MIN && impact <= RISK_MAJOR_MAX) {
            return "MAJOR";
        }
        return "NEGLIGIBLE";
    }

    private String determineStability(final String bugRisk, final String featureRisk,
            final String uiRisk, final double bugImpact,
            final double featureImpact, final double uiImpact) {

        final boolean hasSignificantRisk = bugRisk.equals("SIGNIFICANT")
                || bugRisk.equals("MAJOR")
                || featureRisk.equals("SIGNIFICANT")
                || featureRisk.equals("MAJOR")
                || uiRisk.equals("SIGNIFICANT")
                || uiRisk.equals("MAJOR");

        final boolean allNegligible = bugRisk.equals("NEGLIGIBLE")
                && featureRisk.equals("NEGLIGIBLE")
                && uiRisk.equals("NEGLIGIBLE");

        final boolean allImpactBelow50 = bugImpact < IMPACT_THRESHOLD
                && featureImpact < IMPACT_THRESHOLD
                && uiImpact < IMPACT_THRESHOLD;

        if (hasSignificantRisk) {
            return "UNSTABLE";
        }

        if (allNegligible && allImpactBelow50) {
            return "STABLE";
        }

        return "PARTIALLY STABLE";
    }

    /**
     * Calculates ticket risk metrics for all tickets in the database.
     *
     * @return A list containing risk statistics.
     */
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

        for (final Ticket ticket : DB.getTickets()) {
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
                default:
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
                default:
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
        featureRequestImpact = (MathUtil.round(MathUtil
                .average(featureRequestImpact, featureRequestCount)));
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
