package cloud.codestore.client;

import javax.annotation.Nonnull;

/**
 * Represents a programming language.
 */
public record Language(@Nonnull String name, @Nonnull String id) {
    @Override
    public String toString() {
        return name;
    }
}