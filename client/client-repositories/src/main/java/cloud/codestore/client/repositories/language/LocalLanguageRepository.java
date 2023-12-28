package cloud.codestore.client.repositories.language;

import cloud.codestore.client.Language;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * A repository which loads the available programming languages from the local {CodeStore} Core.
 */
@Repository
class LocalLanguageRepository implements ReadLanguagesUseCase {
    private final HttpClient client;

    public LocalLanguageRepository(HttpClient client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public List<Language> readLanguages() {
        String url = client.getLanguageCollectionUrl();
        var resourceCollection = client.getCollection(url, LanguageResource.class);
        return Arrays.stream(resourceCollection.getData(LanguageResource.class))
                     .map(resource -> new Language(resource.getName(), resource.getId()))
                     .toList();
    }
}
