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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The code controller")
class SnippetCodeTest extends AbstractUiTest {
    @Mock
    private ReadLanguagesUseCase readLanguagesUseCase;
    private SnippetCode controller;

    @Start
    private void start(Stage stage) throws Exception {
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
    void loadLanguages(FxRobot robot) {
        assertThat(languageSelection(robot).getItems()).hasSize(3);
    }

    @Test
    @DisplayName("sets the editability of the code and language selection")
    void setCodeEditable(FxRobot robot) {
        var textField = textField(robot);
        var comboBox = languageSelection(robot);

        robot.interact(() -> controller.setEditing(true));
        assertThat(textField.isEditable()).isTrue();
        assertThat(comboBox.isEditable()).isTrue();

        robot.interact(() -> controller.setEditing(false));
        assertThat(textField.isEditable()).isFalse();
        assertThat(comboBox.isEditable()).isFalse();
    }

    @Test
    @DisplayName("sets the code and language of the given snippet")
    void setDescription(FxRobot robot) {
        Snippet snippet = new SnippetBuilder().uri("")
                                              .code("print(\"Hello, World!\");")
                                              .language(new Language("Python", "1"))
                                              .build();

        robot.interact(() -> controller.visit(snippet));

        assertThat(textField(robot)).hasText(snippet.getCode());
        Language selectedLanguage = languageSelection(robot).getSelectionModel().getSelectedItem();
        assertThat(selectedLanguage).isEqualTo(snippet.getLanguage());
    }

    @Test
    @DisplayName("reads the code and language into the given snippet builder")
    void readDescription(FxRobot robot) {
        String code = "print(\"Hello, World!\");";
        Language language = new Language("Python", "1");
        robot.interact(() -> {
            textField(robot).setText(code);
            languageSelection(robot).getSelectionModel().select(language);
        });

        SnippetBuilder builder = new SnippetBuilder().uri("");
        controller.visit(builder);

        Snippet snippet = builder.build();
        assertThat(snippet.getCode()).isEqualTo(code);
        assertThat(snippet.getLanguage()).isEqualTo(language);
    }

    private ComboBox<Language> languageSelection(FxRobot robot) {
        return robot.lookup("#languageSelection").queryComboBox();
    }

    private TextInputControl textField(FxRobot robot) {
        return robot.lookup("#snippetCode").queryTextInputControl();
    }
}