package services;

import repositories.UserRepository;
import users.User;
import users.Reporter;
import users.Developer;
import users.Manager;
import io.UserInput;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getUser(String username) {
        return userRepository.findById(username).orElse(null);
    }
    
    public boolean userExists(String username) {
        return userRepository.findById(username).isPresent();
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    public void loadUsersFromInput(List<UserInput> inputs) {
        for (UserInput input : inputs) {
            User user = switch (input.role()) {
                case "REPORTER" -> new Reporter(input.username(), input.email(), input.role());
                case "DEVELOPER" -> new Developer(input.username(), input.email(), input.role(), 
                    input.hireDate(), input.expertiseArea(), input.seniority());
                case "MANAGER" -> new Manager(input.username(), input.email(), input.role(), 
                    input.hireDate(), input.subordinates());
                default -> throw new IllegalArgumentException("Unknown role");
            };
            userRepository.save(user);
        }
    }
    
    public List<User> getSubordinates(String managerUsername) {
        return userRepository.getSubordinates(managerUsername);
    }
}
