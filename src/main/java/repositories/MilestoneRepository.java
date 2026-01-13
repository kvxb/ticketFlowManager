package repositories;

import milestones.Milestone;
import java.util.*;
import java.util.stream.Collectors;

public class MilestoneRepository implements Repository<Milestone, String> {
    private Map<String, Milestone> milestones = new HashMap<>();

    @Override
    public Optional<Milestone> findById(String name) {
        return Optional.ofNullable(milestones.get(name));
    }

    @Override
    public List<Milestone> findAll() {
        return new ArrayList<>(milestones.values());
    }

    @Override
    public void save(Milestone milestone) {
        milestones.put(milestone.getName(), milestone);
    }

    @Override
    public void delete(String name) {
        milestones.remove(name);
    }

    public List<Milestone> findByOwner(String owner) {
        return milestones.values().stream()
                .filter(m -> owner.equals(m.getOwner()))
                .collect(Collectors.toList());
    }

    public Optional<Milestone> findContainingTicket(int ticketId) {
        return milestones.values().stream()
                .filter(milestone -> milestone.containsTicket(ticketId))
                .findFirst();
    }
}
