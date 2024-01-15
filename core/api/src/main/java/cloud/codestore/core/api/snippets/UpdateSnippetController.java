package cloud.codestore.core.api.snippets;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippet;
import cloud.codestore.core.usecases.updatesnippet.UpdatedSnippetDto;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.error.ErrorSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class UpdateSnippetController {
    @Nonnull
    private final SnippetDeserializationHelper deserializationHelper;
    private final ReadSnippet readSnippetUseCase;
    private final UpdateSnippet updateSnippetUseCase;

    public UpdateSnippetController(
            @Nonnull SnippetDeserializationHelper deserializationHelper,
            @Nonnull ReadSnippet readSnippetUseCase,
            @Nonnull UpdateSnippet updateSnippetUseCase
    ) {
        this.deserializationHelper = deserializationHelper;
        this.readSnippetUseCase = readSnippetUseCase;
        this.updateSnippetUseCase = updateSnippetUseCase;
    }

    @PatchMapping("/{snippetId}")
    public JsonApiDocument updateSnippet(
            @PathVariable("snippetId") String snippetId,
            @RequestBody SingleResourceDocument<SnippetResource> document
    ) throws SnippetNotExistsException, LanguageNotExistsException, InvalidSnippetException {
        SnippetResource resource = document.getData();
        if (!Objects.equals(resource.getId(), snippetId)) {
            throw new InvalidSnippetIdException();
        }

        UpdatedSnippetDto dto = new UpdatedSnippetDto(
                snippetId,
                deserializationHelper.getLanguage(resource.getLanguage()),
                resource.getTitle(),
                resource.getCode(),
                Collections.emptyList(),
                resource.getDescription()
        );

        updateSnippetUseCase.update(dto);

        var snippet = readSnippetUseCase.read(snippetId);
        return new SnippetResource(snippet).asDocument();
    }

    @PostMapping(value = "/{snippetId}", headers = "X-HTTP-Method-Override=PATCH")
    public JsonApiDocument updateSnippetViaPost(
            @PathVariable("snippetId") String snippetId,
            @RequestBody SingleResourceDocument<SnippetResource> document
    ) throws SnippetNotExistsException, LanguageNotExistsException, InvalidSnippetException {
        return updateSnippet(snippetId, document);
    }

    private static class InvalidSnippetIdException extends RuntimeException {}

    @ExceptionHandler(InvalidSnippetIdException.class)
    public ResponseEntity<Object> nonExistingObject() {
        ErrorResponseBuilder responseBuilder = new ErrorResponseBuilder();
        ErrorObject errorObject = responseBuilder.createError("INVALID_SNIPPET", "invalidSnippet.title", "invalidId.detail");
        errorObject.setSource(new ErrorSource().setPointer("/data/id"));
        return responseBuilder.createResponse(HttpStatus.BAD_REQUEST, errorObject);
    }
}
