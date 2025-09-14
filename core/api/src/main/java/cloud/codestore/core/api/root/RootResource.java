package cloud.codestore.core.api.root;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.api.languages.LanguageCollectionResource;
import cloud.codestore.core.api.snippets.SnippetCollectionResource;
import cloud.codestore.core.api.tags.TagCollectionResource;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Represents the root resource of the {CodeStore} Core API.
 */
class RootResource extends ResourceObject {
    private static final String API_VERSION = "1.0";

    RootResource() {
        super("core", "1");
        setSelfLink(UriFactory.createUri(""));
    }

    @JsonGetter("name")
    String getName() {
        return "{CodeStore} Core";
    }

    @JsonGetter("apiVersion")
    String getApiVersion() {
        return API_VERSION;
    }

    @JsonGetter("apiVersionHeader")
    String getApiVersionHeader() {
        return "X-API-Version";
    }

    @JsonGetter("documentation")
    String getDocumentationUrl() {
        return "https://codestore.cloud/api-documentation";
    }

    @JsonGetter("snippets")
    Relationship getSnippets() {
        return new Relationship(SnippetCollectionResource.createLink());
    }

    @JsonGetter("languages")
    Relationship getLanguages() {
        return new Relationship(LanguageCollectionResource.createLink());
    }

    @JsonGetter("tags")
    Relationship getTags() {
        return new Relationship(TagCollectionResource.createLink());
    }
}
