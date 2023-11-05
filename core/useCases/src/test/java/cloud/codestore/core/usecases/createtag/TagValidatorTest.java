package cloud.codestore.core.usecases.createtag;

import cloud.codestore.core.DefaultLocale;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@ExtendWith(DefaultLocale.class)
@DisplayName("A tag is invalid if")
class TagValidatorTest {
    private final TagValidator validator = new TagValidator();

    @Test
    @DisplayName("the tag is empty")
    void empty() {
        validate("", "The tag must not be empty.");
    }

    @Test
    @DisplayName("the tag is too long")
    void tooLong() throws InvalidTagException {
        validator.validate(RandomString.make(30));
        validate(RandomString.make(31), "The tag must not be longer than 30 characters.");
    }

    @Test
    @DisplayName("the tag contains whitespaces")
    void containsWhitespace() {
        validate("test tag", "The tag must not contain whitespaces.");
    }

    private void validate(String tag, String errorMessage) {
        var exception = catchThrowableOfType(() -> validator.validate(tag), InvalidTagException.class);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
    }
}