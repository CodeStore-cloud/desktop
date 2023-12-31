package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Permission;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.api.Operation;
import cloud.codestore.core.api.ResourceMetaInfo;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadSnippetController {
    private final ReadSnippet readSnippetUseCase;

    @Autowired
    public ReadSnippetController(@Nonnull ReadSnippet readSnippetUseCase) {
        this.readSnippetUseCase = readSnippetUseCase;
    }

    @GetMapping("/{snippetId}")
    public JsonApiDocument getSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippet = readSnippetUseCase.read(snippetId);
        var snippetResource = new SnippetResource(snippet);
        var metaInfo = createMetaInfo(snippet.getPermissions(), snippetResource.getSelfLink());
        return snippetResource.setMeta(metaInfo).asDocument();
    }

    @Nullable
    private ResourceMetaInfo createMetaInfo(Set<Permission> permissions, String snippetUri) {
        List<Operation> operations = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission == Permission.DELETE) {
                operations.add(new Operation("deleteSnippet", HttpMethod.DELETE.name(), snippetUri));
            }
        }

        return operations.isEmpty() ? null : new ResourceMetaInfo(operations);
    }
}
