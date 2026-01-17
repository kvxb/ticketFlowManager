package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;

public class CommentLengthHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(final CommandInput command) {
        return true;
    }
    
    @Override
    protected boolean validateCondition(final CommandInput command) {
        final String comment = command.comment();
        return comment.length() >= 10;
    }
    
    @Override
    protected void showError(final CommandInput command) {
        IOUtil.commentError(command, "MIN_LENGTH");
    }
}
