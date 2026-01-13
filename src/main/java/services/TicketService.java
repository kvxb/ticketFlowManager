package services;

import repositories.TicketRepository;
import repositories.MilestoneRepository;
import tickets.Ticket;
import tickets.Bug;
import tickets.FeatureRequest;
import tickets.UIFeedback;
import io.CommandInput;
import io.IOUtil;
import validation.commenthandlers.CommentValidationHandler;
import validation.commenthandlers.TicketExistenceHandler;
import validation.commenthandlers.AnonymousTicketHandler;
import validation.commenthandlers.ClosedTicketHandler;
import validation.commenthandlers.CommentLengthHandler;
import validation.commenthandlers.DeveloperAssignmentHandler;
import validation.commenthandlers.ReporterOwnershipHandler;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class TicketService {
    private final TicketRepository ticketRepository;
    
    public TicketService(TicketRepository ticketRepository, 
                         MilestoneRepository milestoneRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    public Ticket getTicket(int id) {
        return ticketRepository.findById(id).orElse(null);
    }
    
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
    
    public List<Ticket> getAssignedTickets(String username) {
        return ticketRepository.findByAssignedTo(username).stream()
            .sorted(Comparator
                .comparing(Ticket::getBusinessPriority).reversed()
                .thenComparing(Ticket::getId))
            .collect(Collectors.toList());
    }
    
    public void addTicket(CommandInput command) {
        if (!command.params().type().equals("BUG") && command.params().reportedBy().isEmpty()) {
            IOUtil.ticketError(command, "ANON");
            return;
        }
        if (!command.params().reportedBy().isEmpty() && !command.params().reportedBy().equals(command.username())) {
            IOUtil.ticketError(command, "NUSR");
            return;
        }
        
        Ticket ticket = switch (command.params().type()) {
            case "BUG" -> new Bug.Builder()
                .id(Ticket.getNextTicketId())
                .title(command.params().title())
                .type(command.params().type())
                .businessPriority(
                    command.params().reportedBy().isEmpty()
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
                    ? Integer.parseInt(command.params().errorCode())
                    : 0)
                .createdAt(command.timestamp())
                .build();
                
            case "FEATURE_REQUEST" -> new FeatureRequest.Builder()
                .id(Ticket.getNextTicketId())
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
                .id(Ticket.getNextTicketId())
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
                
            default -> throw new IllegalArgumentException(
                "Unknown ticket type: " + command.params().type());
        };
        
        ticketRepository.save(ticket);
    }
    
    public void addComment(CommandInput command) {
        Ticket ticket = getTicket(command.ticketID());
        if (ticket == null) return;
        
        CommentValidationHandler validateComment = new TicketExistenceHandler();
        validateComment.setNext(new AnonymousTicketHandler())
                .setNext(new ClosedTicketHandler())
                .setNext(new CommentLengthHandler())
                .setNext(new DeveloperAssignmentHandler())
                .setNext(new ReporterOwnershipHandler());
        
        boolean isValid = validateComment.validate(command);
        if (!isValid) return;
        
        ticket.addComment(command.username(), command.comment(), command.timestamp());
        ticketRepository.save(ticket);
    }
}
