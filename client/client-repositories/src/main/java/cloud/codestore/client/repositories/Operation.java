package cloud.codestore.client.repositories;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * Represents an operation which is allowed to be executed on the corresponding resource.
 *
 * @param name the name of this operation.
 */
public record Operation(String name) {
    @JsonCreator
    public Operation(@JsonProperty("operation") @Nonnull String name) {
        this.name = name;
    }
}
