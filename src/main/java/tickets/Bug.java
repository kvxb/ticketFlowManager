package tickets;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import mathutils.MathUtil;

/**
 * Represents a Bug ticket in the system.
 */
public final class Bug extends Ticket {
    private static final double SCORE_RARE = 1.0;
    private static final double SCORE_OCCASIONAL = 2.0;
    private static final double SCORE_FREQUENT = 3.0;
    private static final double SCORE_ALWAYS = 4.0;
    private static final double SCORE_DEFAULT = -1.0;

    private static final double SCORE_LOW = 1.0;
    private static final double SCORE_MEDIUM = 2.0;
    private static final double SCORE_HIGH = 3.0;
    private static final double SCORE_CRITICAL = 4.0;

    private static final double SCORE_MINOR = 1.0;
    private static final double SCORE_MODERATE = 2.0;
    private static final double SCORE_SEVERE = 3.0;

    private static final double IMPACT_NORM = 48.0;
    private static final double EFFICIENCY_SCALE = 10.0;
    private static final double EFFICIENCY_NORM = 70.0;
    private static final double RISK_NORM = 12.0;

    private final String expectedBehaviour;
    private final String actualBehaviour;

    @Override
    public double getImpact() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> SCORE_RARE;
            case "OCCASIONAL" -> SCORE_OCCASIONAL;
            case "FREQUENT" -> SCORE_FREQUENT;
            case "ALWAYS" -> SCORE_ALWAYS;
            default -> SCORE_DEFAULT;
        };

        final double priorityScore = switch (businessPriority.name()) {
            case "LOW" -> SCORE_LOW;
            case "MEDIUM" -> SCORE_MEDIUM;
            case "HIGH" -> SCORE_HIGH;
            case "CRITICAL" -> SCORE_CRITICAL;
            default -> SCORE_DEFAULT;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> SCORE_MINOR;
            case "MODERATE" -> SCORE_MODERATE;
            case "SEVERE" -> SCORE_SEVERE;
            default -> SCORE_DEFAULT;
        };
        return MathUtil.normalize(frequencyScore * priorityScore * severityScore, IMPACT_NORM);
    }

    @Override
    public double getEfficiency() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> SCORE_RARE;
            case "OCCASIONAL" -> SCORE_OCCASIONAL;
            case "FREQUENT" -> SCORE_FREQUENT;
            case "ALWAYS" -> SCORE_ALWAYS;
            default -> SCORE_DEFAULT;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> SCORE_MINOR;
            case "MODERATE" -> SCORE_MODERATE;
            case "SEVERE" -> SCORE_SEVERE;
            default -> SCORE_DEFAULT;
        };

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((frequencyScore + severityScore) * EFFICIENCY_SCALE)
                / daysToResolve, EFFICIENCY_NORM);
    }

    @Override
    public double getRisk() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> SCORE_RARE;
            case "OCCASIONAL" -> SCORE_OCCASIONAL;
            case "FREQUENT" -> SCORE_FREQUENT;
            case "ALWAYS" -> SCORE_ALWAYS;
            default -> SCORE_DEFAULT;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> SCORE_MINOR;
            case "MODERATE" -> SCORE_MODERATE;
            case "SEVERE" -> SCORE_SEVERE;
            default -> SCORE_DEFAULT;
        };
        return MathUtil.normalize(frequencyScore * severityScore, RISK_NORM);
    }

    /**
     * Enum representing the frequency of a bug.
     */
    public enum Frequency {
        RARE,
        OCCASIONAL,
        FREQUENT,
        ALWAYS
    }

    /**
     * Enum representing the severity of a bug.
     */
    public enum Severity {
        MINOR,
        MODERATE,
        SEVERE
    }

    private final Frequency frequency;
    private final Severity severity;
    private final String environment;
    private final Integer errorCode;

    private Bug(final Builder builder) {
        super(builder);
        this.expectedBehaviour = builder.expectedBehaviour;
        this.actualBehaviour = builder.actualBehaviour;
        this.frequency = builder.frequency;
        this.severity = builder.severity;
        this.environment = builder.environment;
        this.errorCode = builder.errorCode;
    }

    /**
     * Builder class for creating Bug tickets.
     */
    public static final class Builder extends Ticket.Builder<Builder> {
        private String expectedBehaviour;
        private String actualBehaviour;
        private Frequency frequency;
        private Severity severity;
        private String environment;
        private int errorCode;

        /**
         * Default constructor initializing type to BUG.
         */
        public Builder() {
            super.type("BUG");
        }

        /**
         * Sets the expected behaviour.
         *
         * @param value the expected behaviour description
         * @return the builder instance
         */
        public Builder expectedBehaviour(final String value) {
            this.expectedBehaviour = value;
            return this;
        }

        /**
         * Sets the actual behaviour.
         *
         * @param value the actual behaviour description
         * @return the builder instance
         */
        public Builder actualBehaviour(final String value) {
            this.actualBehaviour = value;
            return this;
        }

        /**
         * Sets the frequency of the bug.
         *
         * @param value the frequency
         * @return the builder instance
         */
        public Builder frequency(final Frequency value) {
            this.frequency = value;
            return this;
        }

        /**
         * Sets the severity of the bug.
         *
         * @param value the severity
         * @return the builder instance
         */
        public Builder severity(final Severity value) {
            this.severity = value;
            return this;
        }

        /**
         * Sets the environment where the bug occurred.
         *
         * @param value the environment description
         * @return the builder instance
         */
        public Builder environment(final String value) {
            this.environment = value;
            return this;
        }

        /**
         * Sets the error code associated with the bug.
         *
         * @param value the error code
         * @return the builder instance
         */
        public Builder errorCode(final Integer value) {
            this.errorCode = value;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Bug build() {
            return new Bug(this);
        }

    }

}
