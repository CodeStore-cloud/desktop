package cloud.codestore.core.api.root;

import cloud.codestore.jsonapi.meta.MetaInformation;
import com.fasterxml.jackson.annotation.JsonProperty;

class JsonApiMetaInformation implements MetaInformation {
    @JsonProperty("documentation")
    public final String documentation = "https://jsonapi.org/format/1.1/";
}
