package cloud.codestore.core.repositories.serialization;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PersistentSnippetDto {
    private int language;
    private String title;
    private String description;
    private String code;
    private List<String> tags;
    private String created;
    private String modified;
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonCreator
    PersistentSnippetDto(
            @JsonProperty("language") int language,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("code") String code,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("created") String created,
            @JsonProperty("modified") String modified
    ) {
        this.language = language;
        this.title = title;
        this.description = description;
        this.code = code;
        this.tags = tags;
        this.created = created;
        this.modified = modified;
    }

    PersistentSnippetDto(
            int language,
            String title,
            String description,
            String code,
            List<String> tags,
            String created,
            String modified,
            Map<String, Object> additionalProperties
    ) {
        this.language = language;
        this.title = title;
        this.description = description;
        this.code = code;
        this.tags = tags;
        this.created = created;
        this.modified = modified;
        this.additionalProperties = additionalProperties;
    }

    @JsonGetter("language")
    int getLanguage() {
        return language;
    }

    @JsonGetter("title")
    String getTitle() {
        return title;
    }

    @JsonGetter("description")
    String getDescription() {
        return description;
    }

    @JsonGetter("code")
    String getCode() {
        return code;
    }

    @JsonGetter("tags")
    List<String> getTags() {
        return tags;
    }

    @JsonGetter("created")
    String getCreated() {
        return created;
    }

    @JsonGetter("modified")
    String getModified() {
        return modified;
    }

    @JsonAnySetter
    void add(String property, Object value) {
        additionalProperties.put(property, value);
    }

    @JsonAnyGetter
    Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}