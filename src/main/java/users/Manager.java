package users;

import java.util.List;

public class Manager extends User {
    private String hireDate;
    private String[] subordinates;

    public Manager(String username, String email, String role, String hireDate, String[] subordinates) {
        super(username, email, role);
        this.hireDate = hireDate;

        this.subordinates = subordinates;
        // be careful is this a move or a copy or whatever when you implement idk if
        // ava even supports move semantics
    }
}
