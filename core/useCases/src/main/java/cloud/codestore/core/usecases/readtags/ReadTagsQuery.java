package cloud.codestore.core.usecases.readtags;

import java.util.Collection;

public interface ReadTagsQuery {
    /**
     * @return all available tags.
     */
    Collection<String> read();
}
