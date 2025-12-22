package ticket;

public class UIFeedback {
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
