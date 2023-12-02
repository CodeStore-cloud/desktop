package cloud.codestore.core.api;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.createtag.InvalidTagException;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.jsonapi.error.ErrorObject;
import cloud.codestore.jsonapi.error.ErrorSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
class ErrorHandler extends ErrorResponseBuilder {
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
        ErrorObject errorObject = createError("NOT_FOUND", "notFound.title", "notFound.detail.snippet");
        return createResponse(HttpStatus.NOT_FOUND, errorObject);
    }

    @ExceptionHandler(TagNotExistsException.class)
    public ResponseEntity<Object> tagNotExists(TagNotExistsException exception) {
        ErrorObject errorObject = createError("NOT_FOUND", "notFound.title", "notFound.detail.tag", exception.getTag());
        return createResponse(HttpStatus.NOT_FOUND, errorObject);
    }

    @ExceptionHandler(InvalidTagException.class)
    public ResponseEntity<Object> invalidTag(InvalidTagException exception) {
        ErrorSource errorSource = new ErrorSource().setPointer("/data/attributes/name");
        ErrorObject errorObject = new ErrorObject().setCode("INVALID_TAG")
                                                   .setTitle(message("invalidTag.title"))
                                                   .setDetail(exception.getMessage())
                                                   .setSource(errorSource);

        return createResponse(HttpStatus.BAD_REQUEST, errorObject);
    }

    @ExceptionHandler(LanguageNotExistsException.class)
    public ResponseEntity<Object> languageNotExists() {
        ErrorObject errorObject = createError("NOT_FOUND", "notFound.title", "notFound.detail.language");
        return createResponse(HttpStatus.NOT_FOUND, errorObject);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> invalidParameter(InvalidParameterException exception) {
        ErrorSource errorSource = new ErrorSource().setParameter(exception.getParameterName());
        ErrorObject errorObject = new ErrorObject().setCode("INVALID_PARAMETER")
                                                   .setTitle(message("invalidParameter.title"))
                                                   .setDetail(message("invalidParameter.detail", exception.getParameterName()))
                                                   .setSource(errorSource);

        return createResponse(HttpStatus.BAD_REQUEST, errorObject);
    }
}
