package tickets;


final public class Bug extends Ticket {
    private String expectedBehaviour;
    private String actualBehaviour;
    
    
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

    private Frequency frequency;
    private Severity severity;
    private String environment;
    private Integer errorCode;

    private Bug(Builder builder) {
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

            public Builder expectedBehaviour(String expectedBehaviour) {
                this.expectedBehaviour = expectedBehaviour;
                return this;
            }

            public Builder actualBehaviour(String actualBehaviour) {
                this.actualBehaviour = actualBehaviour;
                return this;
            }

            public Builder frequency(Frequency frequency) {
                this.frequency = frequency;
                return this;
            }

            public Builder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public Builder environment(String environment) {
                this.environment = environment;
                return this;
            }

            public Builder errorCode(Integer errorCode) {
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

