package cloud.codestore.core.api.root;

import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.JsonApiObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/", produces = JsonApiDocument.MEDIA_TYPE)
public class RootController {
    @GetMapping
    public JsonApiDocument getRootResource() {
        return new RootResource()
                .asDocument()
                .setJsonapiObject(new JsonApiObject(new JsonApiMetaInformation()));
    }
}
