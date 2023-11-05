package cloud.codestore.core.api.tags;

import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = TagCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadTagController {
    private final ReadTags readTagsUseCase;

    public ReadTagController(ReadTags readTagsUseCase) {
        this.readTagsUseCase = readTagsUseCase;
    }

    @GetMapping("/{tag}")
    public JsonApiDocument getTag(@PathVariable("tag") String tagId) throws TagNotExistsException {
        if (!readTagsUseCase.readTags().contains(tagId)) {
            throw new TagNotExistsException(tagId);
        }

        return new TagResource(tagId).asDocument();
    }
}
