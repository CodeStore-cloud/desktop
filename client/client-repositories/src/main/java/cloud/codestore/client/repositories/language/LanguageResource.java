package cloud.codestore.client.repositories.language;

import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LanguageResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "language";

    private String name;

    @JsonCreator
    LanguageResource(@JsonProperty("name") String name) {
        super(RESOURCE_TYPE);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
