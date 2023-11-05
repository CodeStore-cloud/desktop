package cloud.codestore.core.api.tags;

import cloud.codestore.core.usecases.createtag.CreateTag;
import cloud.codestore.core.usecases.createtag.InvalidTagException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = TagCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class CreateTagController {
    private final CreateTag createTagUseCase;

    public CreateTagController(CreateTag createTagUseCase) {
        this.createTagUseCase = createTagUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonApiDocument createTag(
            @RequestBody SingleResourceDocument<TagResource> document,
            HttpServletResponse response
    ) throws InvalidTagException {
        String tag = document.getData().getName();
        createTagUseCase.create(tag);

        TagResource tagResource = new TagResource(tag);
        response.setHeader(HttpHeaders.LOCATION, tagResource.getSelfLink());
        return tagResource.asDocument();
    }
}
