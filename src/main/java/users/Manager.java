package users;

import java.util.List;

public class Manager extends User {
    private String hireDate;
    private String[] subordinates;

    public Manager(final String username, final String email, final String role, final String hireDate,
            final String[] subordinates) {
        super(username, email, role);
        this.hireDate = hireDate;

        this.subordinates = subordinates;
        // be careful is this a move or a copy or whatever when you implement idk if
        // ava even supports move semantics
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(final String hireDate) {
        this.hireDate = hireDate;
    }

    public String[] getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(final String[] subordinates) {
        this.subordinates = subordinates;
    }
}
