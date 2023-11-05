package cloud.codestore.core.repositories.tags;

import cloud.codestore.core.repositories.Repository;
import cloud.codestore.core.usecases.createtag.CreateTagQuery;
import cloud.codestore.core.usecases.readtags.ReadTagsQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Repository
public class TagRepository implements ReadTagsQuery, CreateTagQuery {
    private final Set<String> tags = new HashSet<>();

    @Override
    public Collection<String> read() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void create(String tag) {
        tags.add(tag);
    }

    public void add(Collection<String> tags) {
        this.tags.addAll(tags);
    }
}
