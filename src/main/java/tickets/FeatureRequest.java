package tickets;

import mathutils.MathUtil;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

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

    @Override
    public double getImpact() {
        final double businessScore = switch (businessValue.name()) {
            case "S" -> 1.0;
            case "M" -> 3.0;
            case "L" -> 6.0;
            case "XL" -> 10.0;
            default -> -1.0;
        };

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 3.0;
            case "HIGH" -> 6.0;
            case "VERY_HIGH" -> 10.0;
            default -> -1.0;
        };

        return MathUtil.normalize(businessScore * customerScore, 100);
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

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 3.0;
            case "HIGH" -> 6.0;
            case "VERY_HIGH" -> 10.0;
            default -> -1.0;
        };

        return MathUtil.normalize(businessScore + customerScore, 20);
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

        final double customerScore = switch (customerDemand.name()) {
            case "LOW" -> 1.0;
            case "MEDIUM" -> 3.0;
            case "HIGH" -> 6.0;
            case "VERY_HIGH" -> 10.0;
            default -> -1.0;
        };

        int daysToResolve = -(int) ChronoUnit.DAYS.between(LocalDate.parse(this.getSolvedAt()),
                LocalDate.parse(this.getAssignedAt()));
        daysToResolve++;

        return MathUtil.normalize(((businessScore + customerScore) / daysToResolve), 20);
    }

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    private FeatureRequest(final Builder builder) {
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

        public Builder businessValue(final BusinessValue businessValue) {
            this.businessValue = businessValue;
            return this;
        }

        public Builder customerDemand(final CustomerDemand customerDemand) {
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
