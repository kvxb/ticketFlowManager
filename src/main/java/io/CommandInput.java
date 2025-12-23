package io;

public record CommandInput (
    String name,
    String command,
    String username,
    String timestamp,
    String dueDate,
    String[] blockingFor,
    String[] assignedDevs,
    int[] tickets,
    int ticketID,
    String comment,
    Filters filters,
    ParamsInput params
) {

}
