package cloud.codestore.client.repositories.tags;

import cloud.codestore.client.repositories.HttpClient;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class TagRepository implements cloud.codestore.client.TagRepository {
    private final HttpClient client;

    TagRepository(HttpClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public List<String> get(String tagsUri) {
        var resourceCollection = client.getCollection(tagsUri, TagResource.class);
        return Arrays.stream(resourceCollection.getData())
                     .map(TagResource::getName)
                     .toList();
    }
}
