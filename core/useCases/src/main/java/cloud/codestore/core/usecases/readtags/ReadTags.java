package cloud.codestore.core.usecases.readtags;

import cloud.codestore.core.UseCase;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Use case: read all available tags.
 */
@UseCase
public class ReadTags {
    private ReadTagsQuery readTagsQuery;

    public ReadTags(ReadTagsQuery readTagsQuery) {
        this.readTagsQuery = readTagsQuery;
    }

    @Nonnull
    public Collection<String> readTags() {
        return readTagsQuery.read();
    }
}
