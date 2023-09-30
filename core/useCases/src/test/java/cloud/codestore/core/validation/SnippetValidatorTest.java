package cloud.codestore.core.validation;

import cloud.codestore.core.DefaultLocale;
import cloud.codestore.core.Snippet;
import net.bytebuddy.utility.RandomString;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static cloud.codestore.core.validation.SnippetProperty.*;

@ExtendWith(DefaultLocale.class)
@DisplayName("A code snippet is invalid if")
class SnippetValidatorTest {
    private final SnippetValidator validator = new SnippetValidator();

    @Test
    @DisplayName("the title is empty")
    void titleEmpty() {
        Snippet validSnippet = Snippet.builder().title(ofLength(1)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasNoValidationMessage(TITLE);

        Snippet invalidSnippet = Snippet.builder().title("").build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasValidationMessage(TITLE, "The code snippet must have a title.");
    }

    @Test
    @DisplayName("the title is longer than 100 characters")
    void titleTooLong() {
        Snippet validSnippet = Snippet.builder().title(ofLength(100)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasNoValidationMessage(TITLE);

        Snippet invalidSnippet = Snippet.builder().title(RandomString.make(101)).build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasValidationMessage(TITLE, "The title must not be longer than 100 characters.");
    }

    @Test
    @DisplayName("the description is longer than 10,000 characters")
    void descriptionTooLong() {
        Snippet validSnippet = Snippet.builder().description(ofLength(10000)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasNoValidationMessage(DESCRIPTION);

        Snippet invalidSnippet = Snippet.builder().description(ofLength(10001)).build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasValidationMessage(DESCRIPTION, "The description must not be longer than 10,000 characters.");
    }

    @DisplayName("the code is empty")
    @Test
    void codeEmpty() {
        Snippet validSnippet = Snippet.builder().code(ofLength(1)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasNoValidationMessage(CODE);

        Snippet invalidSnippet = Snippet.builder().code("").build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasValidationMessage(CODE, "The code snippet must contain code.");
    }

    @DisplayName("the code is longer than 10,000 characters")
    @Test
    void codeTooLong() {
        Snippet validSnippet = Snippet.builder().code(ofLength(10000)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasNoValidationMessage(CODE);

        Snippet invalidSnippet = Snippet.builder().code(ofLength(10001)).build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasValidationMessage(CODE, "The code must not be longer than 10,000 characters.");
    }

    private InvalidSnippetException validate(Snippet snippet) {
        return Assertions.catchThrowableOfType(() -> validator.validate(snippet), InvalidSnippetException.class);
    }

    private String ofLength(int length) {
        return RandomString.make(length);
    }

    private static class InvalidSnippetAssert extends AbstractAssert<InvalidSnippetAssert, InvalidSnippetException> {
        InvalidSnippetAssert(InvalidSnippetException exception) {
            super(exception, InvalidSnippetAssert.class);
        }

        static InvalidSnippetAssert assertThat(InvalidSnippetException exception) {
            return new InvalidSnippetAssert(exception);
        }

        void hasNoValidationMessage(SnippetProperty snippetProperty) {
            Optional.ofNullable(actual).ifPresent(actual -> {
                var validationMessages = actual.getValidationMessages();
                Assertions.assertThat(validationMessages).doesNotContainKey(snippetProperty);
            });
        }

        void hasValidationMessage(SnippetProperty snippetProperty, String message) {
            Assertions.assertThat(actual).isNotNull();
            var validationMessages = actual.getValidationMessages();
            Assertions.assertThat(validationMessages).isNotNull().isNotEmpty();
            Assertions.assertThat(validationMessages).containsKey(snippetProperty);
            Assertions.assertThat(validationMessages.get(snippetProperty)).isEqualTo(message);
        }
    }
}