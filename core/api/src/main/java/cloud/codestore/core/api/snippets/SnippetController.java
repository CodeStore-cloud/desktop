package cloud.codestore.core.api.snippets;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class SnippetController {
    private ListSnippets listSnippetsUseCase;
    private ReadSnippet readSnippetUseCase;

    @Autowired
    public SnippetController(
            ListSnippets listSnippetsUseCase,
            ReadSnippet readSnippetUseCase
    ) {
        this.listSnippetsUseCase = listSnippetsUseCase;
        this.readSnippetUseCase = readSnippetUseCase;
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
}
