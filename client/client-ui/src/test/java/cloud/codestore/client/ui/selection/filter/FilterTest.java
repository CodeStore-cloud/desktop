package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@DisplayName("The filter controller")
class FilterTest extends AbstractUiTest {
    @Mock
    private ReadLanguagesUseCase readLanguagesUseCase;
    @Mock
    private EventBus eventBus;
    @InjectMocks
    private Filter controller;

    @Start
    private void start(Stage stage) throws Exception {
        lenient().when(readLanguagesUseCase.readLanguages()).thenReturn(List.of(
                new Language("Python", "1"),
                new Language("HTML", "2"),
                new Language("Java", "3"),
                new Language("Kotlin", "4")
        ));

        start(stage, "filter.fxml", controller);
    }

    @Test
    @DisplayName("triggers a FilterEvent when the tags changed")
    void tagsChanged(FxRobot robot) {
        var argument = ArgumentCaptor.forClass(FilterEvent.class);

        tagsInput(robot).setText("hello world");

        verify(eventBus).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties();
        assertThat(filterProperties.getTags()).isNotEmpty();
        assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("hello", "world");
    }

    @Test
    @DisplayName("triggers a FilterEvent without tags when the tag input is empty")
    void emptyTagInput(FxRobot robot) {
        var argument = ArgumentCaptor.forClass(FilterEvent.class);

        TextInputControl inputField = tagsInput(robot);
        inputField.setText("test");
        inputField.setText("");

        verify(eventBus, times(2)).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties(); // second call
        assertThat(filterProperties.getTags()).isEmpty();
    }

    @Test
    @DisplayName("triggers a FilterEvent when the programming language changed")
    void languageChanged(FxRobot robot) {
        var argument = ArgumentCaptor.forClass(FilterEvent.class);

        var comboBox = languageSelection(robot);
        assertThat(comboBox.getItems()).hasSize(5);
        assertThat(comboBox.getItems().get(0)).isEqualTo(new LanguageItem(null, "All"));

        robot.interact(() -> comboBox.getSelectionModel().select(3));

        verify(eventBus).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties();
        assertThat(filterProperties.getLanguage()).isNotEmpty();
        assertThat(filterProperties.getLanguage().get()).isEqualTo(new Language("Java", "3"));
    }

    private TextInputControl tagsInput(FxRobot robot) {
        return robot.lookup("#tagsInput").queryTextInputControl();
    }

    private ComboBox<LanguageItem> languageSelection(FxRobot robot) {
        return robot.lookup("#languageSelection").queryComboBox();
    }
}