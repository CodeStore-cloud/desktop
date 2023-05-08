package cloud.codestore.core;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Abstract domain exception that indicates that a business rule was violated.
 */
public abstract class CoreException extends Exception {
    protected CoreException() {}

    protected CoreException(String message) {
        super(message);
    }

    protected static String createMessage(String messageKey, Object... messageArguments) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

        if (messageArguments.length == 0)
            return resourceBundle.getString(messageKey);

        MessageFormat formatter = new MessageFormat(resourceBundle.getString(messageKey));
        return formatter.format(messageArguments);
    }
}
