package tickets;

import mathutils.MathUtil;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

/**
 * Represents a Feature Request ticket in the system.
 */
public final class FeatureRequest extends Ticket {

    private static final double SCORE_S = 1.0;
    private static final double SCORE_M = 3.0;
    private static final double SCORE_L = 6.0;
    private static final double SCORE_XL = 10.0;

    private static final double SCORE_LOW = 1.0;
    private static final double SCORE_MEDIUM = 3.0;
    private static final double SCORE_HIGH = 6.0;
    private static final double SCORE_VERY_HIGH = 10.0;

    private static final double SCORE_DEFAULT = -1.0;

    private static final double NORM_IMPACT = 100.0;
    private static final double NORM_RISK = 20.0;
    private static final double NORM_EFFICIENCY = 20.0;

    /**
     * Enum for Business Value classification.
     */
    public enum BusinessValue {
        S,
        M,
        L,
        XL
    }

    /**
     * Enum for Customer Demand classification.
     */
    public enum CustomerDemand {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }

    @Override
    public double getImpact() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> SCORE_S;
            case "M" -> SCORE_M;
            case "L" -> SCORE_L;
            case "XL" -> SCORE_XL;
            default -> SCORE_DEFAULT;
        };

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> SCORE_LOW;
            case "MEDIUM" -> SCORE_MEDIUM;
            case "HIGH" -> SCORE_HIGH;
            case "VERY_HIGH" -> SCORE_VERY_HIGH;
            default -> SCORE_DEFAULT;
        };

        return MathUtil.normalize(businessScore * customerScore, NORM_IMPACT);
    }

    @Override
    public double getRisk() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> SCORE_S;
            case "M" -> SCORE_M;
            case "L" -> SCORE_L;
            case "XL" -> SCORE_XL;
            default -> SCORE_DEFAULT;
        };

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> SCORE_LOW;
            case "MEDIUM" -> SCORE_MEDIUM;
            case "HIGH" -> SCORE_HIGH;
            case "VERY_HIGH" -> SCORE_VERY_HIGH;
            default -> SCORE_DEFAULT;
        };

        return MathUtil.normalize(businessScore + customerScore, NORM_RISK);
    }

    @Override
    public double getEfficiency() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> SCORE_S;
            case "M" -> SCORE_M;
            case "L" -> SCORE_L;
            case "XL" -> SCORE_XL;
            default -> SCORE_DEFAULT;
        };

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> SCORE_LOW;
            case "MEDIUM" -> SCORE_MEDIUM;
            case "HIGH" -> SCORE_HIGH;
            case "VERY_HIGH" -> SCORE_VERY_HIGH;
            default -> SCORE_DEFAULT;
        };

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((businessScore + customerScore) / daysToResolve),
                NORM_EFFICIENCY);
    }

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    private FeatureRequest(final Builder builder) {
        super(builder);
        this.businessValue = builder.businessValue;
        this.customerDemand = builder.customerDemand;
    }

    /**
     * Builder class for constructing FeatureRequest objects.
     */
    public static final class Builder extends Ticket.Builder<Builder> {
        private BusinessValue businessValue;
        private CustomerDemand customerDemand;

        /**
         * Default constructor initializing the ticket type.
         */
        public Builder() {
            super.type("FEATURE_REQUEST");
        }

        /**
         * Sets the business value.
         *
         * @param val the business value to set
         * @return the builder instance
         */
        public Builder businessValue(final BusinessValue val) {
            this.businessValue = val;
            return this;
        }

        /**
         * Sets the customer demand.
         *
         * @param val the customer demand to set
         * @return the builder instance
         */
        public Builder customerDemand(final CustomerDemand val) {
            this.customerDemand = val;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FeatureRequest build() {
            return new FeatureRequest(this);
        }

    }

}
