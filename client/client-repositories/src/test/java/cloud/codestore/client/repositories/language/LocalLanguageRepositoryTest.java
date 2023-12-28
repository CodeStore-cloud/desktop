package cloud.codestore.client.repositories.language;

import cloud.codestore.client.Language;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The local language repository")
class LocalLanguageRepositoryTest {
    private static final String LANGUAGES_URL = "http://localhost:8080/languages";

    @Mock
    private HttpClient client;
    @InjectMocks
    private LocalLanguageRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(client.getLanguageCollectionUrl()).thenReturn(LANGUAGES_URL);
    }

    @Test
    @DisplayName("retrieves all programming languages from the core")
    void retrieveAllLanguages() {
        LanguageResource[] resources = testResources();
        var resourceCollection = new ResourceCollectionDocument<>(resources);
        when(client.getCollection(LANGUAGES_URL, LanguageResource.class)).thenReturn(resourceCollection);

        List<Language> languages = repository.readLanguages();

        assertThat(languages).isNotNull().containsExactly(
                new Language("HTML", "2"),
                new Language("Java", "1"),
                new Language("Kotlin", "4"),
                new Language("Python", "3")
        );
    }

    private LanguageResource[] testResources() {
        return new LanguageResource[]{
                testResource("2", "HTML"),
                testResource("1", "Java"),
                testResource("4", "Kotlin"),
                testResource("3", "Python")
        };
    }

    private LanguageResource testResource(String id, String name) {
        var langauge = mock(LanguageResource.class);
        when(langauge.getName()).thenReturn(name);
        when(langauge.getId()).thenReturn(id);
        return langauge;
    }
}