package cloud.codestore.client.repositories.tags;

import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The tag repository")
class TagRepositoryTest {
    private static final String TAGS_URI = "http://localhost:8080/tags?filter[snippet]=1";

    @Mock
    private HttpClient client;
    private TagRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TagRepository(client);
    }

    @Test
    @DisplayName("retrieves a list of tags from the core")
    void retrieveTagList() {
        TagResource[] testTags = new TagResource[]{tag("TagA"),tag("TagB"),tag("TagC")};
        var resourceCollection = new ResourceCollectionDocument<>(testTags);
        when(client.getCollection(TAGS_URI, TagResource.class)).thenReturn(resourceCollection);

        List<String> tags = repository.get(TAGS_URI);

        assertThat(tags).isNotNull().containsExactlyInAnyOrder("TagA", "TagB", "TagC");
    }

    private TagResource tag(String name) {
        return new TagResource(name);
    }
}