package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.AbstractUiTest;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The snippet footer")
class FooterTest extends AbstractUiTest {
    private Footer controller = new Footer();

    @Start
    private void start(Stage stage) throws Exception {
        start(stage, "footer.fxml", controller);
    }

    @Test
    @DisplayName("shows the delete-button if permitted")
    void showDeleteButton(FxRobot robot) {
        var button = deleteButton(robot);
        button.setPrefSize(30, 30);

        controller.setPermissions(Set.of(Permission.DELETE));
        assertThat(button.isVisible()).isTrue();

        controller.setPermissions(Collections.emptySet());
        assertThat(button.isVisible()).isFalse();
    }

    @Test
    @DisplayName("calls the callback of the delete-button")
    void deleteCallback(FxRobot robot) {
        var callback = mock(Runnable.class);
        controller.onDelete(callback);
        robot.clickOn(deleteButton(robot));
        verify(callback).run();
    }

    private Button deleteButton(FxRobot robot) {
        return robot.lookup("#deleteButton").queryButton();
    }
}