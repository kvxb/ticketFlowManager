package tickets;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import mathutils.MathUtil;

final public class Bug extends Ticket {
    private final String expectedBehaviour;
    private final String actualBehaviour;

    @Override
    public double getImpact() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> 1.0;
            case "OCCASIONAL" -> 2.0;
            case "FREQUENT" -> 3.0;
            case "ALWAYS" -> 4.0;
            default -> -1.0;
        };

        final double priorityScore = switch (businessPriority.name()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 2.0;
            case "HIGH" -> 3.0;
            case "CRITICAL" -> 4.0;
            default -> -1.0;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> 1.0;
            case "MODERATE" -> 2.0;
            case "SEVERE" -> 3.0;
            default -> -1.0;
        };
        return MathUtil.normalize(frequencyScore * priorityScore * severityScore, 48);
    }

    @Override
    public double getEfficiency() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> 1.0;
            case "OCCASIONAL" -> 2.0;
            case "FREQUENT" -> 3.0;
            case "ALWAYS" -> 4.0;
            default -> -1.0;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> 1.0;
            case "MODERATE" -> 2.0;
            case "SEVERE" -> 3.0;
            default -> -1.0;
        };

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((frequencyScore + severityScore) * 10 /
                daysToResolve), 70);
    }

    @Override
    public double getRisk() {
        final double frequencyScore = switch (frequency.name()) {
            case "RARE" -> 1.0;
            case "OCCASIONAL" -> 2.0;
            case "FREQUENT" -> 3.0;
            case "ALWAYS" -> 4.0;
            default -> -1.0;
        };

        final double severityScore = switch (severity.name()) {
            case "MINOR" -> 1.0;
            case "MODERATE" -> 2.0;
            case "SEVERE" -> 3.0;
            default -> -1.0;
        };
        return MathUtil.normalize(frequencyScore * severityScore, 12);
    }

    public enum Frequency {
        RARE,
        OCCASIONAL,
        FREQUENT,
        ALWAYS
    }

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

    public static class Builder extends Ticket.Builder<Builder> {
        private String expectedBehaviour;
        private String actualBehaviour;
        private Frequency frequency;
        private Severity severity;
        private String environment;
        private int errorCode;

        public Builder() {
            super.type("BUG");
        }

        public Builder expectedBehaviour(final String expectedBehaviour) {
            this.expectedBehaviour = expectedBehaviour;
            return this;
        }

        public Builder actualBehaviour(final String actualBehaviour) {
            this.actualBehaviour = actualBehaviour;
            return this;
        }

        public Builder frequency(final Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder severity(final Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder environment(final String environment) {
            this.environment = environment;
            return this;
        }

        public Builder errorCode(final Integer errorCode) {
            this.errorCode = errorCode;
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
