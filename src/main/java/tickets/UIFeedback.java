package tickets;

import tickets.FeatureRequest.BusinessValue;

public class UIFeedback extends Ticket {
    private String uiElementId;
    private int usabilityScore;// 1-10
    private String screenshotUrl;
    private String suggestedFix;

    public enum businessValue {
        S,
        M,
        L,
        XL
    }

    private BusinessValue businessValue;

    private UIFeedback(Builder builder) {
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

        public Builder uiElementId(String uiElementId) {
            this.uiElementId = uiElementId;
            return this;
        }

        public Builder usabilityScore(int usabilityScore) {
            this.usabilityScore = usabilityScore;
            return this;
        }

        public Builder screenshotUrl(String screenshotUrl) {
            this.screenshotUrl = screenshotUrl;
            return this;
        }

        public Builder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }

        public Builder businessValue(BusinessValue businessValue) {
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
