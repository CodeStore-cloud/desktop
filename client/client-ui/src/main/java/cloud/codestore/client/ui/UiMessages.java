package cloud.codestore.client.ui;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class UiMessages {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("uiMessages");

    /**
     * @return the underlying {@link ResourceBundle} which contains the UI texts.
     */
    static ResourceBundle bundle() {
        return MESSAGES;
    }

    /**
     * Returns the message for the given key.
     * If one or more arguments are given, the message is formatted as defined in {@link MessageFormat}.
     *
     * @param key       a message key. Must not be {@code null}.
     * @param arguments optional arguments to pass into the message.
     * @return the formatted message from the resource bundle or the given key if there is no corresponding message.
     */
    public static String get(@Nonnull String key, Object... arguments) {
        try {
            String message = MESSAGES.getString(key);
            return arguments.length > 0 ? new MessageFormat(message).format(arguments) : message;

        } catch (MissingResourceException e) {
            return key;
        }
    }
}
