package cloud.codestore.core.api;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ResourceBundle;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("error");

    @ExceptionHandler(SnippetNotExistsException.class)
    public ResponseEntity<Object> snippetNotExists(SnippetNotExistsException exception, WebRequest request) {
        ErrorObject errorObject = new ErrorObject().setCode("NOT_FOUND")
                                                   .setTitle(message("notFound.title"))
                                                   .setDetail(message("notFound.detail.snippet"));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE);

        ErrorDocument responseBody = new ErrorDocument(errorObject);
        return handleExceptionInternal(exception, responseBody, headers, HttpStatus.NOT_FOUND, request);
    }

    private String message(String messageKey) {
        return resourceBundle.getString(messageKey);
    }
}
