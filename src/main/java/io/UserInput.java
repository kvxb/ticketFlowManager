package io;

public record UserInput (
    String username,
    String role,
    String email,
    String hireDate,
    String seniority,
    String expertiseArea,
    String[] subordinates
) {

}
