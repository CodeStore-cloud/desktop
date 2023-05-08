package cloud.codestore.core.validation;

import cloud.codestore.core.DefaultLocale;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import net.bytebuddy.utility.RandomString;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DefaultLocale.class)
@DisplayName("A code snippet is invalid if")
class SnippetValidatorTest {
    private final SnippetValidator validator = new SnippetValidator();

    @Test
    @DisplayName("the title is empty")
    void titleEmpty() {
        Snippet validSnippet = new SnippetBuilder().title(ofLength(1)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasValidProperty("title");

        Snippet invalidSnippet = new SnippetBuilder().build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasInvalidProperty("title", "The code snippet must have a title.");
    }

    @Test
    @DisplayName("the title is too long")
    void titleTooLong() {
        Snippet validSnippet = new SnippetBuilder().title(ofLength(100)).build();
        InvalidSnippetAssert.assertThat(validate(validSnippet)).hasValidProperty("title");

        Snippet invalidSnippet = new SnippetBuilder().title(RandomString.make(101)).build();
        InvalidSnippetAssert.assertThat(validate(invalidSnippet))
                            .hasInvalidProperty("title", "The title must not be longer than 100 characters.");
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

        void hasValidProperty(String propertyName) {
            Assertions.assertThat(actual).isNotNull();
            var validationMessages = actual.getValidationMessages();
            Assertions.assertThat(validationMessages).doesNotContainKey(propertyName);
        }

        void hasInvalidProperty(String propertyName, String message) {
            Assertions.assertThat(actual).isNotNull();
            var validationMessages = actual.getValidationMessages();
            Assertions.assertThat(validationMessages).isNotNull().isNotEmpty();
            Assertions.assertThat(validationMessages).containsKey(propertyName);
            Assertions.assertThat(validationMessages.get(propertyName)).isEqualTo(message);
        }
    }
}