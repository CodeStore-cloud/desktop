package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Method;
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
        controller = new SnippetCode(readLanguagesUseCase);
        start(stage, "code.fxml", controller);
    }

    @Test
    @DisplayName("loads the available programming languages from the core")
    void loadLanguages(FxRobot robot) throws Exception {
        var languages = new Language[]{
                new Language("Java", "1"),
                new Language("Text", "2"),
                new Language("HTML", "3")
        };
        when(readLanguagesUseCase.readLanguages()).thenReturn(List.of(languages));

        callInitialize();

        assertThat(languageSelection(robot).getItems()).containsExactly(languages);
    }

    private ComboBox<Language> languageSelection(FxRobot robot) {
        return robot.lookup("#languageSelection").queryComboBox();
    }

    private void callInitialize() throws Exception {
        Method method = SnippetCode.class.getDeclaredMethod("initialize");
        method.setAccessible(true);
        method.invoke(controller);
    }
}