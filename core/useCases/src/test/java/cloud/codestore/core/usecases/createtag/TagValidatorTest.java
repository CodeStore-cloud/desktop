package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.DefaultLocale;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(DefaultLocale.class)
@DisplayName("A tag is invalid if")
class TagValidatorTest {
    private final TagValidator validator = new TagValidator();

    @Test
    @DisplayName("the tag is empty")
    void empty() {
        expectError("", "The tag must not be empty.");
    }

    @Test
    @DisplayName("the tag is too long")
    void tooLong() {
        assertThatNoException().isThrownBy(() -> validator.validate(RandomString.make(30)));
        expectError(RandomString.make(31), "The tag must not be longer than 30 characters.");
    }

    @Test
    @DisplayName("the tag contains whitespaces")
    void containsWhitespace() {
        expectError("test tag", "The tag must not contain whitespaces.");
    }

    private void expectError(String tag, String errorMessage) {
        assertThatThrownBy(() -> validator.validate(tag))
                .isInstanceOf(InvalidTagException.class)
                .hasMessage(errorMessage);
    }
}