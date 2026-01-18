package tickets;

import tickets.FeatureRequest.BusinessValue;
import mathutils.MathUtil;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;

/**
 * Represents a UI Feedback ticket.
 */
public final class UIFeedback extends Ticket {
    private static final double SCORE_S = 1.0;
    private static final double SCORE_M = 3.0;
    private static final double SCORE_L = 6.0;
    private static final double SCORE_XL = 10.0;
    private static final double SCORE_DEFAULT = -1.0;

    private static final double NORM_IMPACT = 100.0;
    private static final double NORM_RISK = 100.0;
    private static final double NORM_EFFICIENCY = 20.0;

    private static final int MAX_USABILITY_FACTOR = 11;

    private final String uiElementId;
    private final int usabilityScore; // 1-10
    private final String screenshotUrl;
    private final String suggestedFix;
    private final BusinessValue businessValue;

    @Override
    public double getImpact() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> SCORE_S;
            case "M" -> SCORE_M;
            case "L" -> SCORE_L;
            case "XL" -> SCORE_XL;
            default -> SCORE_DEFAULT;
        };

        return MathUtil.normalize(businessScore * usabilityScore, NORM_IMPACT);
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

        return MathUtil.normalize(businessScore * (MAX_USABILITY_FACTOR - usabilityScore),
                NORM_RISK);
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

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((businessScore + usabilityScore) / daysToResolve),
                NORM_EFFICIENCY);
    }

    private UIFeedback(final Builder builder) {
        super(builder);
        this.uiElementId = builder.uiElementId;
        this.usabilityScore = builder.usabilityScore;
        this.screenshotUrl = builder.screenshotUrl;
        this.suggestedFix = builder.suggestedFix;
        this.businessValue = builder.businessValue;
    }

    /**
     * Builder for UIFeedback tickets.
     */
    public static final class Builder extends Ticket.Builder<Builder> {
        private String uiElementId;
        private int usabilityScore; // 1-10
        private String screenshotUrl;
        private String suggestedFix;
        private BusinessValue businessValue;

        /**
         * Default constructor.
         */
        public Builder() {
            super.type("FEATURE_REQUEST");
        }

        /**
         * Sets the UI element ID.
         *
         * @param val the UI element ID
         * @return the builder instance
         */
        public Builder uiElementId(final String val) {
            this.uiElementId = val;
            return this;
        }

        /**
         * Sets the usability score.
         *
         * @param val the usability score (1-10)
         * @return the builder instance
         */
        public Builder usabilityScore(final int val) {
            this.usabilityScore = val;
            return this;
        }

        /**
         * Sets the screenshot URL.
         *
         * @param val the screenshot URL
         * @return the builder instance
         */
        public Builder screenshotUrl(final String val) {
            this.screenshotUrl = val;
            return this;
        }

        /**
         * Sets the suggested fix.
         *
         * @param val the suggested fix description
         * @return the builder instance
         */
        public Builder suggestedFix(final String val) {
            this.suggestedFix = val;
            return this;
        }

        /**
         * Sets the business value.
         *
         * @param val the business value
         * @return the builder instance
         */
        public Builder businessValue(final BusinessValue val) {
            this.businessValue = val;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public UIFeedback build() {
            return new UIFeedback(this);
        }

    }

}
