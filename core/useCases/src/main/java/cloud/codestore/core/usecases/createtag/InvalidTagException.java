package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.CoreException;

/**
 * Exception in case an invalid tag should be created.
 */
public class InvalidTagException extends CoreException {
    InvalidTagException(String message) {
        super(createMessage(message));
    }
}
