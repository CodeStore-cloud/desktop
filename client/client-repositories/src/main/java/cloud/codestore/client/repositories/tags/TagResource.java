package cloud.codestore.client.repositories.tags;

import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TagResource extends ResourceObject {
    public static final String RESOURCE_TYPE = "tag";

    private String name;

    @JsonCreator
    public TagResource(@JsonProperty("name") String name) {
        super(RESOURCE_TYPE);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
