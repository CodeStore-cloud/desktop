package cloud.codestore.core.usecases.readtags;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.TagNotExistsException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * Use case: read all available tags.
 */
@Injectable
public class ReadTags {
    private final ReadTagsQuery readTagsQuery;

    public ReadTags(ReadTagsQuery readTagsQuery) {
        this.readTagsQuery = readTagsQuery;
    }

    @Nonnull
    public Collection<String> readTags() {
        return readTagsQuery.read();
    }

    @Nonnull
    public Collection<String> readTags(@Nonnull String... tags) throws TagNotExistsException {
        var availableTags = readTags();
        for (String tag : tags) {
            if (!availableTags.contains(tag)) {
                throw new TagNotExistsException(tag);
            }
        }

        return Set.of(tags);
    }
}
