package input;

public record CommandInput (
    String command,
    String username,
    String timestamp,
    ParamsInput paramsInput
) {

}
