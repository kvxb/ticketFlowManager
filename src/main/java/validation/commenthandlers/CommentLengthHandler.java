package validation.commenthandlers;

import io.CommandInput;
import io.IOUtil;

public class CommentLengthHandler extends CommentValidationHandler {
    @Override
    protected boolean appliesTo(CommandInput command) {
        return true;
    }
    
    @Override
    protected boolean validateCondition(CommandInput command) {
        String comment = command.comment();
        return comment.length() >= 10;
    }
    
    @Override
    protected void showError(CommandInput command) {
        IOUtil.commentError(command, "MIN_LENGTH");
    }
}
