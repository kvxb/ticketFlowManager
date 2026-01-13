package repositories;

import tickets.Ticket;
import java.util.*;
import java.util.stream.Collectors;

public class TicketRepository implements Repository<Ticket, Integer> {
    private Map<Integer, Ticket> tickets = new HashMap<>();
    
    @Override
    public Optional<Ticket> findById(Integer id) {
        return Optional.ofNullable(tickets.get(id));
    }
    
    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }
    
    @Override
    public void save(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }
    
    @Override
    public void delete(Integer id) {
        tickets.remove(id);
    }
    
    public List<Ticket> findByAssignedTo(String username) {
        return tickets.values().stream()
            .filter(ticket -> username.equals(ticket.getAssignedTo()))
            .collect(Collectors.toList());
    }
    
    public List<Ticket> findByStatus(String status) {
        return tickets.values().stream()
            .filter(ticket -> ticket.getStatus().name().equals(status))
            .collect(Collectors.toList());
    }
}
