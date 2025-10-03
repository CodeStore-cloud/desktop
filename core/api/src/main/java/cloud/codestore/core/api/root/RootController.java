package cloud.codestore.core.api.root;

import cloud.codestore.core.usecases.synchronizesnippets.ExecutedSynchronizations;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.JsonApiObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/", produces = JsonApiDocument.MEDIA_TYPE)
public class RootController {
    private final ExecutedSynchronizations executedSynchronizations;

    public RootController(ExecutedSynchronizations executedSynchronizations) {
        this.executedSynchronizations = executedSynchronizations;
    }

    @GetMapping
    public JsonApiDocument getRootResource() {
        var initialSynchronization = executedSynchronizations.getOptionalInitialSynchronization().orElse(null);
        return new RootResource(initialSynchronization)
                .asDocument()
                .setJsonapiObject(new JsonApiObject(new JsonApiMetaInformation()));
    }
}
