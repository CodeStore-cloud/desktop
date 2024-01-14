package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The code controller")
class SnippetCodeTest extends AbstractUiTest {
    @Mock
    private ReadLanguagesUseCase readLanguagesUseCase;
    private SnippetCode controller;

    @Start
    public void start(Stage stage) throws Exception {
        var languages = new Language[]{
                new Language("Python", "1"),
                new Language("Text", "2"),
                new Language("HTML", "3")
        };
        when(readLanguagesUseCase.readLanguages()).thenReturn(List.of(languages));
        controller = new SnippetCode(readLanguagesUseCase);
        start(stage, "code.fxml", controller);
    }

    @Test
    @DisplayName("loads the available programming languages from the core")
    void loadLanguages() {
        assertThat(languageSelection().getItems()).hasSize(3);
    }

    @Test
    @DisplayName("sets the editability of the code and language selection")
    void setCodeEditable() {
        var textField = textField();
        var comboBox = languageSelection();

        interact(() -> controller.setEditing(true));
        assertThat(textField.isEditable()).isTrue();
        assertThat(comboBox.isEditable()).isTrue();

        interact(() -> controller.setEditing(false));
        assertThat(textField.isEditable()).isFalse();
        assertThat(comboBox.isEditable()).isFalse();
    }

    @Test
    @DisplayName("sets the code and language of the given snippet")
    void setDescription() {
        Snippet snippet = Snippet.builder()
                                 .code("print(\"Hello, World!\");")
                                 .language(new Language("Python", "1"))
                                 .build();

        interact(() -> controller.visit(snippet));

        assertThat(textField()).hasText(snippet.getCode());
        Language selectedLanguage = languageSelection().getSelectionModel().getSelectedItem();
        assertThat(selectedLanguage).isEqualTo(snippet.getLanguage());
    }

    @Test
    @DisplayName("reads the code and language into the given snippet builder")
    void readDescription() {
        String code = "print(\"Hello, World!\");";
        Language language = new Language("Python", "1");
        interact(() -> {
            textField().setText(code);
            languageSelection().getSelectionModel().select(language);
        });

        SnippetBuilder builder = Snippet.builder();
        controller.visit(builder);

        Snippet snippet = builder.build();
        assertThat(snippet.getCode()).isEqualTo(code);
        assertThat(snippet.getLanguage()).isEqualTo(language);
    }

    private ComboBox<Language> languageSelection() {
        return lookup("#languageSelection").queryComboBox();
    }

    private TextInputControl textField() {
        return lookup("#snippetCode").queryTextInputControl();
    }
}