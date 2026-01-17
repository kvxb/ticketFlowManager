package validation.commenthandlers;

import io.CommandInput;
import database.Database;

public abstract class CommentValidationHandler {
    protected CommentValidationHandler next;
    protected Database db = Database.getInstance();

    public CommentValidationHandler setNext(final CommentValidationHandler next) {
        this.next = next;
        return next;
    }

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

    protected abstract boolean appliesTo(CommandInput command);

    protected abstract boolean validateCondition(CommandInput command);

    protected abstract void showError(CommandInput command);
}
