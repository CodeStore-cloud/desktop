package cloud.codestore.core.api;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.TagNotExistsException;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

    @ExceptionHandler(InvalidSnippetException.class)
    public ResponseEntity<Object> invalidSnippet(InvalidSnippetException exception, WebRequest request) {
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


        return createResponse(exception, request, HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(SnippetNotExistsException.class)
    public ResponseEntity<Object> snippetNotExists(SnippetNotExistsException exception, WebRequest request) {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.snippet"));

        return notFound(exception, request, errorObject);
    }

    @ExceptionHandler(TagNotExistsException.class)
    public ResponseEntity<Object> tagNotExists(TagNotExistsException exception, WebRequest request) {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.tag", exception.getTag()));

        return notFound(exception, request, errorObject);
    }

    @ExceptionHandler(LanguageNotExistsException.class)
    public ResponseEntity<Object> languageNotExists(LanguageNotExistsException exception, WebRequest request) {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.language"));

        return notFound(exception, request, errorObject);
    }

    private ResponseEntity<Object> notFound(Exception exception, WebRequest request, ErrorObject... errorObjects) {
        return createResponse(exception, request, HttpStatus.NOT_FOUND, errorObjects);
    }

    private ResponseEntity<Object> createResponse(
            Exception exception, WebRequest request, HttpStatus httpStatus, ErrorObject... errorObjects
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE);

        ErrorDocument responseBody = new ErrorDocument(errorObjects);
        return handleExceptionInternal(exception, responseBody, headers, httpStatus, request);
    }

    private String message(String messageKey, String... messageArguments) {
        if (messageArguments.length == 0)
            return resourceBundle.getString(messageKey);

        MessageFormat formatter = new MessageFormat(resourceBundle.getString(messageKey));
        return formatter.format(messageArguments);
    }
}
