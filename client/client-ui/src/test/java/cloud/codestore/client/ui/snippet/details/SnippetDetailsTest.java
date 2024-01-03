package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The details controller")
class SnippetDetailsTest extends AbstractUiTest {
    private SnippetDetails controller = new SnippetDetails();

    @Start
    private void start(Stage stage) throws Exception {
        start(stage, "details.fxml", controller);
    }

    @Test
    @DisplayName("shows the given tags")
    void showTags(FxRobot robot) {
        controller.setTags(List.of("A", "B", "C"));
        Assertions.assertThat(tagsInput(robot).getText()).isEqualTo("A B C");
    }

    @Test
    @DisplayName("provides a list of tags from the input")
    void readTags(FxRobot robot) {
        tagsInput(robot).setText("A B C");
        Assertions.assertThat(controller.getTags()).containsExactly("A", "B", "C");
    }

    private TextInputControl tagsInput(FxRobot robot) {
        return robot.lookup("#tagsInput").queryTextInputControl();
    }
}