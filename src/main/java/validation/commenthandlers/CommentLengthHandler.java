package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;

/**
 * Checks if a comment meets the min length required
 */
public final class CommentLengthHandler extends CommentValidationHandler {
    private static final int MIN_COMMENT_LENGTH = 10;

    @Override
    protected boolean appliesTo(final CommandInput command) {
        return true;
    }

    @Override
    protected boolean validateCondition(final CommandInput command) {
        final String comment = command.comment();
        return comment.length() >= MIN_COMMENT_LENGTH;
    }

    @Override
    protected void showError(final CommandInput command) {
        IOUtil.commentError(command, "MIN_LENGTH");
    }
}
