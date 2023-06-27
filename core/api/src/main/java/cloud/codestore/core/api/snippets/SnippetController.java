package cloud.codestore.core.api.snippets;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippet;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class SnippetController {
    private ListSnippets listSnippetsUseCase;
    private ReadSnippet readSnippetUseCase;
    private DeleteSnippet deleteSnippetUseCase;

    @Autowired
    public SnippetController(
            @Nonnull ListSnippets listSnippetsUseCase,
            @Nonnull ReadSnippet readSnippetUseCase,
            DeleteSnippet deleteSnippetUseCase
    ) {
        this.listSnippetsUseCase = listSnippetsUseCase;
        this.readSnippetUseCase = readSnippetUseCase;
        this.deleteSnippetUseCase = deleteSnippetUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets() {
        var snippets = listSnippetsUseCase.list();
        return new SnippetCollectionResource(snippets);
    }

    @GetMapping("/{snippetId}")
    public JsonApiDocument getSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippet = readSnippetUseCase.read(snippetId);
        return new SnippetResource(snippet).asDocument();
    }

    @DeleteMapping("/{snippetId}")
    public JsonApiDocument deleteSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippetResource = getSnippet(snippetId);
        deleteSnippetUseCase.delete(snippetId);
        return snippetResource;
    }
}
