package cloud.codestore.core.api;

import com.fasterxml.jackson.annotation.JsonGetter;

import javax.annotation.Nonnull;

/**
 * Represents an operation which is allowed to be executed on the corresponding resource.
 * It is always part of a {@link ResourceMetaInfo} object.
 */
public class Operation {
    private final String operation;
    private final String method;
    private final String href;

    public Operation(@Nonnull String operation, @Nonnull String method, @Nonnull String href) {
        this.operation = operation;
        this.method = method;
        this.href = href;
    }

    @JsonGetter("operation")
    public String getOperation() {
        return operation;
    }

    @JsonGetter("method")
    public String getMethod() {
        return method;
    }

    @JsonGetter("href")
    public String getHref() {
        return href;
    }
}
