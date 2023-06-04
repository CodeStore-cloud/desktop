package cloud.codestore.core.usecases.listlanguages;

import cloud.codestore.core.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The list-languages use case")
class ListLanguagesTest {
    private ListLanguages useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListLanguages();
    }

    @Test
    @DisplayName("returns all available programming languages")
    void returnAvailableLanguages() {
        var languages = useCase.list();
        var expectedResult = List.of(Language.values());
        assertThat(languages).isEqualTo(expectedResult);
    }
}