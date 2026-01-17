package tickets;

import tickets.FeatureRequest.BusinessValue;
import mathutils.MathUtil;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;

public class UIFeedback extends Ticket {
    private final String uiElementId;
    private final int usabilityScore;// 1-10
    private final String screenshotUrl;
    private final String suggestedFix;

    public enum businessValue {
        S,
        M,
        L,
        XL
    }

    @Override
    public double getImpact() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> 1.0;
            case "M" -> 3.0;
            case "L" -> 6.0;
            case "XL" -> 10.0;
            default -> -1.0;
        };

        return MathUtil.normalize(businessScore * usabilityScore, 100);
    }

    @Override
    public double getRisk() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> 1.0;
            case "M" -> 3.0;
            case "L" -> 6.0;
            case "XL" -> 10.0;
            default -> -1.0;
        };

        return MathUtil.normalize(businessScore * (11 - usabilityScore), 100);
    }

    @Override
    public double getEfficiency() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> 1.0;
            case "M" -> 3.0;
            case "L" -> 6.0;
            case "XL" -> 10.0;
            default -> -1.0;
        };

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((businessScore + usabilityScore) / daysToResolve), 20);
    }

    private final BusinessValue businessValue;

    private UIFeedback(final Builder builder) {
        super(builder);
        this.uiElementId = builder.uiElementId;
        this.usabilityScore = builder.usabilityScore;
        this.screenshotUrl = builder.screenshotUrl;
        this.suggestedFix = builder.suggestedFix;
        this.businessValue = builder.businessValue;
    }

    public static class Builder extends Ticket.Builder<Builder> {
        private String uiElementId;
        private int usabilityScore;// 1-10
        private String screenshotUrl;
        private String suggestedFix;
        private BusinessValue businessValue;

        public Builder() {
            super.type("FEATURE_REQUEST");
        }

        public Builder uiElementId(final String uiElementId) {
            this.uiElementId = uiElementId;
            return this;
        }

        public Builder usabilityScore(final int usabilityScore) {
            this.usabilityScore = usabilityScore;
            return this;
        }

        public Builder screenshotUrl(final String screenshotUrl) {
            this.screenshotUrl = screenshotUrl;
            return this;
        }

        public Builder suggestedFix(final String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }

        public Builder businessValue(final BusinessValue businessValue) {
            this.businessValue = businessValue;
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
