package cloud.codestore.core.api.snippets;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippet;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class DeleteSnippetController {
    private final DeleteSnippet deleteSnippetUseCase;
    private final ReadSnippet readSnippetUseCase;

    @Autowired
    public DeleteSnippetController(
            @Nonnull DeleteSnippet deleteSnippetUseCase,
            @Nonnull ReadSnippet readSnippetUseCase
    ) {
        this.deleteSnippetUseCase = deleteSnippetUseCase;
        this.readSnippetUseCase = readSnippetUseCase;
    }

    @DeleteMapping("/{snippetId}")
    public JsonApiDocument deleteSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippet = readSnippetUseCase.read(snippetId);
        deleteSnippetUseCase.delete(snippetId);
        return new SnippetResource(snippet).asDocument();
    }
}
