package cloud.codestore.core.repositories.tags;

import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.readtags.ReadTagsQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Repository
public class TagRepository implements ReadTagsQuery {
    private final Set<String> tags = new HashSet<>();

    @Override
    public Set<String> read() {
        return Collections.unmodifiableSet(tags);
    }

    public void add(Collection<String> tags) {
        this.tags.addAll(tags);
    }
}
