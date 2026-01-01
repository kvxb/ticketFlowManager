package tickets;

public class UIFeedback extends Ticket{
    private String uiElementId;
    private int usabilityScore;//1-10
    private String screenshotUrl;
    private String suggestedFix;

    public enum businessValue {
        S,
        M,
        L,
        XL
    }
}
