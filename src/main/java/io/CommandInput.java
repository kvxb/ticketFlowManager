package io;

import java.time.LocalDate;

public record CommandInput(
        String command,
        String username,
        String timestamp,
        LocalDate time,
        String dueDate,
        String[] blockingFor,
        String[] assignedDevs,
        int[] tickets,
        int ticketID,
        String comment,
        FiltersInput filters,
        ParamsInput params,
        String name

) {
    public CommandInput {
        if (timestamp == null) {
            System.out.println("invalid timestamp");
            time = null;
        } else {
            time = LocalDate.parse(timestamp);
        }
    }
}
