package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.usecases.createsnippet.CreateSnippet;
import cloud.codestore.core.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class CreateSnippetController {
    private final SnippetDeserializationHelper deserializationHelper;
    private final CreateSnippet createSnippetUseCase;

    @Autowired
    public CreateSnippetController(
            @Nonnull SnippetDeserializationHelper deserializationHelper,
            @Nonnull CreateSnippet createSnippetUseCase
    ) {
        this.deserializationHelper = deserializationHelper;
        this.createSnippetUseCase = createSnippetUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonApiDocument createSnippet(
            @RequestBody SingleResourceDocument<SnippetResource> document,
            HttpServletResponse response
    ) throws InvalidSnippetException, LanguageNotExistsException {
        SnippetResource resource = document.getData();
        NewSnippetDto dto = new NewSnippetDto(
                deserializationHelper.getLanguage(resource.getLanguage()),
                resource.getTitle(),
                resource.getCode(),
                resource.getDescription()
        );

        Snippet createdSnippet = createSnippetUseCase.create(dto);

        SnippetResource snippetResource = new SnippetResource(createdSnippet);
        response.setHeader(HttpHeaders.LOCATION, snippetResource.getSelfLink());
        return snippetResource.asDocument();
    }
}
