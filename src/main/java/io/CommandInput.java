package io;

public record CommandInput (
    String command,
    String username,
    String timestamp,
    ParamsInput params
) {

}
