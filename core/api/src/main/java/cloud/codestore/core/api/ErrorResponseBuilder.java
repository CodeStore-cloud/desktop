package cloud.codestore.core.api;

import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.error.ErrorDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Helper class for creating error response objects.
 */
public class ErrorResponseBuilder {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("api-error");

    public ResponseEntity<Object> createResponse(HttpStatus httpStatus, ErrorObject... errorObjects) {
        return createResponse(httpStatus, new ErrorDocument(errorObjects));
    }

    public ResponseEntity<Object> createResponse(HttpStatus httpStatus, ErrorDocument responseBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, JsonApiDocument.MEDIA_TYPE);

        return new ResponseEntity<>(responseBody, headers, httpStatus);
    }

    public ErrorObject createError(String code, String title, String detail, String... detailArguments) {
        return new ErrorObject().setCode(code)
                                .setTitle(message(title))
                                .setDetail(message(detail, detailArguments));
    }

    public String message(String messageKey, String... messageArguments) {
        if (messageArguments.length == 0)
            return resourceBundle.getString(messageKey);

        MessageFormat formatter = new MessageFormat(resourceBundle.getString(messageKey));
        return formatter.format(messageArguments);
    }
}
