package tickets;

import io.CommandInput;

/**
 * Factory class for creating Ticket instances based on command input.
 */
public final class TicketFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private TicketFactory() {
    }

    /**
     * Creates a ticket based on the provided command.
     *
     * @param command The command input containing ticket details.
     * @return The created Ticket instance.
     */
    public static Ticket createTicket(final CommandInput command) {
        return switch (command.params().type()) {
            case "BUG" -> new Bug.Builder()
                    .id(Ticket.getTicketId())
                    .title(command.params().title())
                    .type(command.params().type())
                    .businessPriority(command.params().reportedBy().isEmpty()
                            ? Ticket.BusinessPriority.LOW
                            : Ticket.BusinessPriority.valueOf(
                                    command.params().businessPriority().toUpperCase()))
                    .expertiseArea(Ticket.ExpertiseArea.valueOf(
                            command.params().expertiseArea().toUpperCase()))
                    .reportedBy(command.params().reportedBy())
                    .expectedBehaviour(command.params().expectedBehavior())
                    .actualBehaviour(command.params().actualBehavior())
                    .frequency(Bug.Frequency.valueOf(
                            command.params().frequency().toUpperCase()))
                    .severity(Bug.Severity.valueOf(
                            command.params().severity().toUpperCase()))
                    .environment(command.params().environment())
                    .errorCode(command.params().errorCode() != null
                            ? Integer.parseInt(command.params().errorCode()) : 0)
                    .createdAt(command.timestamp())
                    .build();
            case "FEATURE_REQUEST" -> new FeatureRequest.Builder()
                    .id(Ticket.getTicketId())
                    .type(command.params().type())
                    .title(command.params().title())
                    .businessPriority(Ticket.BusinessPriority.valueOf(
                            command.params().businessPriority().toUpperCase()))
                    .expertiseArea(Ticket.ExpertiseArea.valueOf(
                            command.params().expertiseArea().toUpperCase()))
                    .reportedBy(command.params().reportedBy())
                    .businessValue(FeatureRequest.BusinessValue.valueOf(
                            command.params().businessValue().toUpperCase()))
                    .customerDemand(FeatureRequest.CustomerDemand.valueOf(
                            command.params().customerDemand().toUpperCase()))
                    .createdAt(command.timestamp())
                    .build();
            case "UI_FEEDBACK" -> new UIFeedback.Builder()
                    .id(Ticket.getTicketId())
                    .type(command.params().type())
                    .title(command.params().title())
                    .businessPriority(Ticket.BusinessPriority.valueOf(
                            command.params().businessPriority().toUpperCase()))
                    .expertiseArea(Ticket.ExpertiseArea.valueOf(
                            command.params().expertiseArea().toUpperCase()))
                    .reportedBy(command.params().reportedBy())
                    .businessValue(FeatureRequest.BusinessValue.valueOf(
                            command.params().businessValue().toUpperCase()))
                    .uiElementId(command.params().uiElementId())
                    .usabilityScore(command.params().usabilityScore())
                    .screenshotUrl(command.params().screenshotUrl())
                    .suggestedFix(command.params().suggestedFix())
                    .createdAt(command.timestamp())
                    .build();
            default -> throw new IllegalArgumentException("Unknown ticket type");
        };
    }
}
