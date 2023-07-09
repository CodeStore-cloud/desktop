package cloud.codestore.core.api;

import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetProperty;
import cloud.codestore.jsonapi.error.ErrorDocument;
import cloud.codestore.jsonapi.error.ErrorObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(DefaultLocale.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("The error handler")
class ErrorHandlerTest {
    @Mock
    private WebRequest request;
    private ErrorHandler errorHandler = new ErrorHandler();

    @Test
    @DisplayName("returns 404 if a snippet does not exist")
    void snippetNotFound() {
        ResponseEntity<Object> response = errorHandler.snippetNotExists(new SnippetNotExistsException(), request);
        assertNotExists(response, "The code snippet does not exist.");
    }

    @Test
    @DisplayName("returns 404 if a programming language does not exist")
    void languageNotFound() {
        ResponseEntity<Object> response = errorHandler.languageNotExists(new LanguageNotExistsException(), request);
        assertNotExists(response, "The programming language does not exist.");
    }

    private void assertNotExists(ResponseEntity<Object> response, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorObject[] errors = assertErrors(response, 1);

        ErrorObject errorObject = errors[0];
        assertThat(errorObject.getCode()).isEqualTo("NOT_FOUND");
        assertThat(errorObject.getTitle()).isEqualTo("Not Found");
        assertThat(errorObject.getDetail()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("returns 400 if a code snippet is invalid")
    void invalidSnippet() {
        var validationMessages = Map.of(
                SnippetProperty.TITLE, "invalid title",
                SnippetProperty.DESCRIPTION, "invalid description",
                SnippetProperty.CODE, "invalid code"
        );

        var exception = Mockito.mock(InvalidSnippetException.class);
        when(exception.getValidationMessages()).thenReturn(validationMessages);

        ResponseEntity<Object> response = errorHandler.invalidSnippet(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorObject[] errors = assertErrors(response, 3);

        assertInvalidSnippet(errors[0], "invalid title", "/data/attributes/title");
        assertInvalidSnippet(errors[1], "invalid description", "/data/attributes/description");
        assertInvalidSnippet(errors[2], "invalid code", "/data/attributes/code");
    }

    private ErrorObject[] assertErrors(ResponseEntity<Object> response, int expectedErrorCount) {
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(ErrorDocument.class);

        ErrorObject[] errors = ((ErrorDocument) response.getBody()).getErrors();
        assertThat(errors).isNotNull();
        assertThat(errors).hasSize(expectedErrorCount);

        return errors;
    }

    private void assertInvalidSnippet(ErrorObject errorObject, String expectedMessage, String expectedPointer) {
        assertThat(errorObject.getCode()).isEqualTo("INVALID_SNIPPET");
        assertThat(errorObject.getTitle()).isEqualTo("Invalid Code Snippet");
        assertThat(errorObject.getDetail()).isEqualTo(expectedMessage);
        assertThat(errorObject.getSource().getPointer()).isEqualTo(expectedPointer);
    }
}