package cloud.codestore.client;

import javax.annotation.Nonnull;
import java.util.List;

public interface TagRepository {
    /**
     * Reads the list of tags from the given URI.
     * @param tagsUri the URI of the tags to read.
     * @return the corresponding tags.
     */
    @Nonnull
    List<String> get(String tagsUri);
}
