package cloud.codestore.core.api.snippets;

import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadSnippetCollectionController {
    private ListSnippets listSnippetsUseCase;

    @Autowired
    public ReadSnippetCollectionController(@Nonnull ListSnippets listSnippetsUseCase) {
        this.listSnippetsUseCase = listSnippetsUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets() {
        var snippets = listSnippetsUseCase.list();
        return new SnippetCollectionResource(snippets);
    }
}
