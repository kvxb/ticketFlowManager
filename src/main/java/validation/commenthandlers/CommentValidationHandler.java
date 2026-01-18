package validation.commenthandlers;

import io.CommandInput;
import database.Database;

/**
 * Handler for the command : addComment.
 * Implements the Chain of Responsibility pattern.
 */
public abstract class CommentValidationHandler {
    protected CommentValidationHandler next;
    protected Database db = Database.getInstance();

    /**
     * Sets the next handler in the chain.
     *
     * @param nextHandler The next handler to be executed.
     * @return The next handler, allowing for method chaining.
     */
    public CommentValidationHandler setNext(final CommentValidationHandler nextHandler) {
        this.next = nextHandler;
        return nextHandler;
    }

    /**
     * Validates the command against the handler's specific rules.
     * If the rule applies, it checks the condition. If valid, passes to the next handler.
     *
     * @param command The command containing comment details.
     * @return true if the command is valid (or ignored by this handler), false otherwise.
     */
    public boolean validate(final CommandInput command) {
        if (!appliesTo(command)) {
            return next == null || next.validate(command);
        }

        if (!validateCondition(command)) {
            showError(command);
            return false;
        }

        return next == null || next.validate(command);
    }

    /**
     * Checks if this handler applies to the given command context.
     *
     * @param command The command input.
     * @return true if this handler should validate the command.
     */
    protected abstract boolean appliesTo(CommandInput command);

    /**
     * Checks the specific validation condition.
     *
     * @param command The command input.
     * @return true if the condition is met, false if it violates the rule.
     */
    protected abstract boolean validateCondition(CommandInput command);

    /**
     * Output the specific error message associated with this handler.
     *
     * @param command The command input that caused the error.
     */
    protected abstract void showError(CommandInput command);
}
