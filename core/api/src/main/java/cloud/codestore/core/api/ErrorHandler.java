package cloud.codestore.core.api;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.createtag.InvalidTagException;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.error.ErrorSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

@ControllerAdvice
public class ErrorHandler {
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

    @ExceptionHandler(InvalidSnippetException.class)
    public ResponseEntity<Object> invalidSnippet(InvalidSnippetException exception) {
        var errors = exception.getValidationMessages()
                              .entrySet()
                              .stream()
                              .sorted(Map.Entry.comparingByKey())
                              .map(entry -> {
                                  var property = entry.getKey();
                                  var message = entry.getValue();
                                  var errorSource = new ErrorSource().setPointer("/data/attributes/" + property);
                                  return new ErrorObject().setCode("INVALID_SNIPPET")
                                                          .setTitle(message("invalidSnippet.title"))
                                                          .setDetail(message)
                                                          .setSource(errorSource);
                              })
                              .toArray(ErrorObject[]::new);

        return createResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(SnippetNotExistsException.class)
    public ResponseEntity<Object> snippetNotExists() {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.snippet"));

        return notFound(errorObject);
    }

    @ExceptionHandler(TagNotExistsException.class)
    public ResponseEntity<Object> tagNotExists(TagNotExistsException exception) {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.tag", exception.getTag()));

        return notFound(errorObject);
    }

    @ExceptionHandler(InvalidTagException.class)
    public ResponseEntity<Object> invalidTag(InvalidTagException exception) {
        var errorSource = new ErrorSource().setPointer("/data/attributes/name");
        ErrorObject errorObject = new ErrorObject().setCode("INVALID_TAG")
                                                   .setTitle(message("invalidTag.title"))
                                                   .setDetail(exception.getMessage())
                                                   .setSource(errorSource);

        return createResponse(HttpStatus.BAD_REQUEST, errorObject);
    }

    @ExceptionHandler(LanguageNotExistsException.class)
    public ResponseEntity<Object> languageNotExists() {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.language"));

        return notFound(errorObject);
    }

    private ResponseEntity<Object> notFound(ErrorObject... errorObjects) {
        return createResponse(HttpStatus.NOT_FOUND, errorObjects);
    }

    private ResponseEntity<Object> createResponse(HttpStatus httpStatus, ErrorObject... errorObjects) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE);

        ErrorDocument responseBody = new ErrorDocument(errorObjects);
        return new ResponseEntity<>(responseBody, headers, httpStatus);
    }

    private String message(String messageKey, String... messageArguments) {
        if (messageArguments.length == 0)
            return resourceBundle.getString(messageKey);

        MessageFormat formatter = new MessageFormat(resourceBundle.getString(messageKey));
        return formatter.format(messageArguments);
    }
}
