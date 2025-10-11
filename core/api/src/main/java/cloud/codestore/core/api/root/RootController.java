package cloud.codestore.core.api.root;

import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProcess;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.JsonApiObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/", produces = JsonApiDocument.MEDIA_TYPE)
public class RootController {
    private final SynchronizationProcess synchronizationProcess;

    public RootController(SynchronizationProcess synchronizationProcess) {
        this.synchronizationProcess = synchronizationProcess;
    }

    @GetMapping
    public JsonApiDocument getRootResource() {
        return new RootResource(synchronizationProcess)
                .asDocument()
                .setJsonapiObject(new JsonApiObject(new JsonApiMetaInformation()));
    }
}
