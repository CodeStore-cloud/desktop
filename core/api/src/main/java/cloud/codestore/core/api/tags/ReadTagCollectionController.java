package cloud.codestore.core.api.tags;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

@RestController
@RequestMapping(path = TagCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadTagCollectionController {
    private final ReadTags readTagsUseCase;
    private final ReadSnippet readSnippetUseCase;

    public ReadTagCollectionController(ReadTags readTagsUseCase, ReadSnippet readSnippetUseCase) {
        this.readTagsUseCase = readTagsUseCase;
        this.readSnippetUseCase = readSnippetUseCase;
    }

    @GetMapping
    public JsonApiDocument getTags(
            @RequestParam(value = "filter[snippet]", required = false) @Nullable String snippetId
    ) throws SnippetNotExistsException {
        var tags = snippetId == null ? readTagsUseCase.readTags() : readSnippetUseCase.read(snippetId).getTags();
        return new TagCollectionResource(tags);
    }
}
