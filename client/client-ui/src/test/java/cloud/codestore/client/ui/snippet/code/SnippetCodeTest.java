package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebView;
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
        // Editor is loaded synchronously in the tests, so no need to wait until it´s loaded
    }

    @Test
    @DisplayName("loads the available programming languages from the core")
    void loadLanguages() {
        assertThat(languageSelection().getItems()).hasSize(3);
    }

    @Test
    @DisplayName("sets the editability of the code and language selection")
    void setCodeEditable() {
        var comboBox = languageSelection();

        interact(() -> controller.setEditing(true));
        assertBrowserIsEditable(true);
        assertThat(comboBox.isDisabled()).isFalse();

        interact(() -> controller.setEditing(false));
        assertBrowserIsEditable(false);
        assertThat(comboBox.isDisabled()).isTrue();
    }

    private void assertBrowserIsEditable(boolean expectedValue) {
        interact(() -> assertThat(browser().getEngine().executeScript("editor.isEditable();")).isEqualTo(expectedValue));
    }

    @Test
    @DisplayName("sets the code and language of the given snippet")
    void setDescription() {
        Snippet snippet = Snippet.builder()
                                 .code("print(\"Hello, World!\");")
                                 .language(new Language("Python", "1"))
                                 .build();

        interact(() -> controller.visit(snippet));

        Language selectedLanguage = languageSelection().getSelectionModel().getSelectedItem();
        assertThat(selectedLanguage).isEqualTo(snippet.getLanguage());

        interact(() -> {
            var content = browser().getEngine().executeScript("editor.getContent();");
            assertThat(content).isEqualTo(snippet.getCode());
        });
    }

    @Test
    @DisplayName("reads the code and language into the given snippet builder")
    void readDescription() {
        Snippet testSnippet = Snippet.builder()
                                     .code("print(\"Hello, World!\");")
                                     .language(new Language("Python", "1"))
                                     .build();

        interact(() -> controller.visit(testSnippet));

        SnippetBuilder builder = Snippet.builder();
        interact(() -> controller.visit(builder));

        Snippet readSnippet = builder.build();
        assertThat(readSnippet.getCode()).isEqualTo(testSnippet.getCode());
        assertThat(readSnippet.getLanguage()).isEqualTo(testSnippet.getLanguage());
    }

    private ComboBox<Language> languageSelection() {
        return lookup("#languageSelection").queryComboBox();
    }

    private WebView browser() {
        return lookup("#browser").queryAs(WebView.class);
    }
}