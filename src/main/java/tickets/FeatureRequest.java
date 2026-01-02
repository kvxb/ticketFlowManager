package tickets;

public class FeatureRequest extends Ticket {
    public enum BusinessValue {
        S,
        M,
        L,
        XL
    }

    public enum CustomerDemand {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }

    private BusinessValue businessValue;
    private CustomerDemand customerDemand;

    private FeatureRequest(Builder builder) {
        super(builder);
        this.businessValue = builder.businessValue;
        this.customerDemand = builder.customerDemand;
    }

    public static class Builder extends Ticket.Builder<Builder> {
        private BusinessValue businessValue;
        private CustomerDemand customerDemand;

        public Builder() {
            super.type("FEATURE_REQUEST");
        }

        public Builder businessValue(BusinessValue businessValue) {
            this.businessValue = businessValue;
            return this;
        }

        public Builder customerDemand(CustomerDemand customerDemand) {
            this.customerDemand = customerDemand;
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
