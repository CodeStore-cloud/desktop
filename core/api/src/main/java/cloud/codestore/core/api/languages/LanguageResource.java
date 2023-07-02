package cloud.codestore.core.api.languages;

import cloud.codestore.core.Language;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

import javax.annotation.Nonnull;

public class LanguageResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "language";

    private String name;

    LanguageResource(@Nonnull Language language) {
        super(RESOURCE_TYPE, String.valueOf(language.getId()));

        name = language.getName();
        setSelfLink(getLink(language));
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    /**
     * @param language a programming language
     * @return the URI to the language resource with the given id.
     */
    public static String getLink(@Nonnull Language language) {
        return LanguageCollectionResource.getLink() + "/" + language.getId();
    }
}
