package cloud.codestore.core.usecases.readtags;

import java.util.Set;

public interface ReadTagsQuery {
    /**
     * @return all available tags.
     */
    Set<String> read();
}
