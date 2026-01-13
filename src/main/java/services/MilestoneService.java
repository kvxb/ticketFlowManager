package services;

import repositories.MilestoneRepository;
import repositories.TicketRepository;
import milestones.Milestone;
import io.CommandInput;
import java.util.List;

public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final TicketRepository ticketRepository;
    
    public MilestoneService(MilestoneRepository milestoneRepository,
                           TicketRepository ticketRepository) {
        this.milestoneRepository = milestoneRepository;
        this.ticketRepository = ticketRepository;
    }
    
    public Milestone getMilestone(String name) {
        return milestoneRepository.findById(name).orElse(null);
    }
    
    public List<Milestone> getAllMilestones() {
        return milestoneRepository.findAll();
    }
    
    public void addMilestone(CommandInput command) {
    }
    
    public Milestone getMilestoneFromTicketID(int ticketId) {
        return milestoneRepository.findContainingTicket(ticketId)
            .orElse(null);
    }
}
