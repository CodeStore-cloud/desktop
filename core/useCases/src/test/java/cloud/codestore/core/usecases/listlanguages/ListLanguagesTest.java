package cloud.codestore.core.usecases.listlanguages;

import cloud.codestore.core.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The list-languages use case")
class ListLanguagesTest {
    private ListLanguages useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListLanguages();
    }

    @Test
    @DisplayName("returns all available programming languages sorted by name")
    void returnAvailableLanguages() {
        var languages = useCase.list();
        assertThat(languages).containsExactly(sortedList());
    }

    private Language[] sortedList() {
        return new Language[]{
                Language.BATCH,
                Language.C,
                Language.OBJECTIVE_C,
                Language.CSHARP,
                Language.CPP,
                Language.COBOL,
                Language.CLISP,
                Language.CSS,
                Language.DOCKERFILE,
                Language.FORTRAN,
                Language.GO,
                Language.GROOVY,
                Language.HTML,
                Language.JAVA,
                Language.JAVASCRIPT,
                Language.KOTLIN,
                Language.LATEX,
                Language.LUA,
                Language.MATHEMATICA,
                Language.MATLAB,
                Language.PERL,
                Language.PHP,
                Language.PYTHON,
                Language.RUBY,
                Language.SCALA,
                Language.SQL,
                Language.SWIFT,
                Language.TEXT,
                Language.TYPESCRIPT,
                Language.SHELL,
                Language.XML,
                Language.YAML
        };
    }
}