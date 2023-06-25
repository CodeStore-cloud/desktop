package cloud.codestore.core.repositories;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Abstract exception that indicates an error when accessing a repository.
 */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String messageKey, Object... messageArguments) {
        super(createMessage(messageKey, messageArguments));
    }

    public RepositoryException(Throwable cause, String message, Object... messageArguments) {
        super(createMessage(message, messageArguments), cause);
    }

    private static String createMessage(String messageKey, Object... messageArguments) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

        if (messageArguments.length == 0)
            return resourceBundle.getString(messageKey);

        MessageFormat formatter = new MessageFormat(resourceBundle.getString(messageKey));
        return formatter.format(messageArguments);
    }
}
