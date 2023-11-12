package cloud.codestore.core.usecases.readlanguage;

import cloud.codestore.core.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The read-language use case")
class ReadLanguageTest {
    private ReadLanguage useCase = new ReadLanguage();

    @ParameterizedTest
    @MethodSource("languageStream")
    @DisplayName("returns a programming language by its id")
    void returnLanguage(int languageId, Language expectedLanguage) throws LanguageNotExistsException {
        var language = useCase.read(languageId);
        assertThat(language).isEqualTo(expectedLanguage);
    }

    @Test
    @DisplayName("throws a LanguageNotExistsException if there is no programming language for the given id")
    void invalidId() {
        assertThatThrownBy(() -> useCase.read(-1)).isInstanceOf(LanguageNotExistsException.class);
        assertThatThrownBy(() -> useCase.read(Language.values().length)).isInstanceOf(LanguageNotExistsException.class);
    }

    private static Stream<Arguments> languageStream() {
        return Arrays.stream(Language.values())
                     .map(language -> Arguments.of(language.getId(), language));
    }
}