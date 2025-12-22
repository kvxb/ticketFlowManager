package tickets;

final public class Bug {
    private String expectedBehaviour;
    private String actualBehaviour;
    
    
    public enum frequency {
        RARE,
        OCCASIONAL,
        FREQUENT,
        ALWAYS
    }
    public enum severity {
        MINOR,
        MODERATE,
        SEVERE
    }

    private String environment;
    private int errorCode;
}

