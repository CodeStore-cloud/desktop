package cloud.codestore.core.api.languages;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.UriFactory;
import cloud.codestore.jsonapi.document.ResourceCollectionDocument;

import javax.annotation.Nonnull;
import java.util.List;

public class LanguageCollectionResource extends ResourceCollectionDocument<LanguageResource> {
    static final String PATH = "/languages";

    LanguageCollectionResource(@Nonnull List<Language> languages) {
        super(convertToLanguageResource(languages));
    }

    /**
     * @return the URI to the language collection resource.
     */
    public static String getLink() {
        return UriFactory.createUri(PATH);
    }

    private static LanguageResource[] convertToLanguageResource(List<Language> languages) {
        return languages.stream()
                        .map(LanguageResource::new)
                        .toArray(LanguageResource[]::new);
    }
}
