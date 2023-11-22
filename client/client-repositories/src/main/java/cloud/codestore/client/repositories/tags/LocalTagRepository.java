package cloud.codestore.client.repositories.tags;

import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

@Repository
public class LocalTagRepository implements cloud.codestore.client.TagRepository {
    private final HttpClient client;

    LocalTagRepository(HttpClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public List<String> get(String tagsUri) {
        var resourceCollection = client.getCollection(tagsUri, TagResource.class);
        TagResource[] data = resourceCollection.getData(TagResource.class);
        return Arrays.stream(data)
                     .map(TagResource::getName)
                     .toList();
    }
}
