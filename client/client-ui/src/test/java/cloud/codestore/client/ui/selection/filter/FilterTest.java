package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Set;

import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The filter controller")
class FilterTest extends AbstractUiTest {
    @Mock
    private EventBus eventBus;
    @InjectMocks
    private Filter controller;

    @Start
    private void start(Stage stage) throws Exception {
        start(stage, "filter.fxml", controller);
    }

    @Test
    @DisplayName("triggers a FilterEvent when the tags changed")
    void tagsChanged(FxRobot robot) {
        tagsInput(robot).setText("hello world");

        FilterProperties filterProperties = new FilterProperties(Set.of("hello", "world"));
        FilterEvent expectedEvent = new FilterEvent(filterProperties);
        verify(eventBus).post(expectedEvent);
    }

    private TextInputControl tagsInput(FxRobot robot) {
        return robot.lookup("#tagsInput").queryTextInputControl();
    }
}