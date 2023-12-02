package cloud.codestore.core.api;

import javax.annotation.Nonnull;

/**
 * Exception in case a given URL-parameter is invalid.
 */
public class InvalidParameterException extends Exception {
    private final String parameterName;

    public InvalidParameterException(@Nonnull String parameterName) {
        this.parameterName = parameterName;
    }

    @Nonnull
    String getParameterName() {
        return parameterName;
    }
}
