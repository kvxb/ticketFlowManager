package repositories;

import users.User;
import java.util.*;
import java.util.stream.Collectors;

public class UserRepository implements Repository<User, String> {
    private Map<String, User> users = new HashMap<>();
    
    @Override
    public Optional<User> findById(String username) {
        return Optional.ofNullable(users.get(username));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
    }
    
    @Override
    public void delete(String username) {
        users.remove(username);
    }
    
    public List<User> findByRole(String role) {
        return users.values().stream()
            .filter(user -> user.getRole().name().equals(role))
            .collect(Collectors.toList());
    }
    
    public List<User> getSubordinates(String managerUsername) {
        return new ArrayList<>();
    }
}
