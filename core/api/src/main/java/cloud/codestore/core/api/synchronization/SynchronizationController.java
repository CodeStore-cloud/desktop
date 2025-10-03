package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.ErrorResponseBuilder;
import cloud.codestore.core.usecases.synchronizesnippets.ExecutedSynchronizations;
import cloud.codestore.core.usecases.synchronizesnippets.InitialSynchronization;
import cloud.codestore.core.usecases.synchronizesnippets.SnippetSynchronization;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationNotExistsException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = InitialSynchronizationResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
class SynchronizationController {
    private final ExecutedSynchronizations executedSynchronizations;

    @Autowired
    SynchronizationController(ExecutedSynchronizations executedSynchronizations) {
        this.executedSynchronizations = executedSynchronizations;
    }

    @GetMapping("/1")
    public JsonApiDocument getInitialSynchronization() throws SynchronizationNotExistsException {
        InitialSynchronization initialSynchronization = executedSynchronizations.getInitialSynchronization();
        return new InitialSynchronizationResource(initialSynchronization).asDocument();
    }

    @GetMapping("/{snippetId}")
    public JsonApiDocument getSynchronization(
            @PathVariable("snippetId") String snippetId
    ) throws SynchronizationNotExistsException {
        SnippetSynchronization synchronization = executedSynchronizations.get(snippetId);
        return new SnippetSynchronizationResource(snippetId, synchronization).asDocument();
    }

    @ExceptionHandler(SynchronizationNotExistsException.class)
    public ResponseEntity<Object> nonExistingSynchronization() {
        ErrorResponseBuilder responseBuilder = new ErrorResponseBuilder();
        ErrorObject errorObject = responseBuilder.createError("NOT_FOUND", "notFound.title", "notFound.detail.synchronization");
        return responseBuilder.createResponse(HttpStatus.NOT_FOUND, errorObject);
    }
}
