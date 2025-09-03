package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.QuickFilterEvent;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The filter controller")
class FilterTest extends AbstractUiTest {
    private static final Language JAVA = new Language("Java", "3");

    @Mock
    private ReadLanguagesUseCase readLanguagesUseCase;
    private EventBus eventBus = new EventBus();
    private Filter controller;

    @Start
    public void start(Stage stage) throws Exception {
        when(readLanguagesUseCase.readLanguages()).thenReturn(List.of(
                new Language("Python", "1"),
                new Language("HTML", "2"),
                JAVA,
                new Language("Kotlin", "4")
        ));

        controller = new Filter(readLanguagesUseCase, eventBus);
        start(stage, "filter.fxml", controller);
        eventBus.post(new ApplicationReadyEvent());
        controller.toggle();
    }

    @Test
    @DisplayName("hides the filterPanel")
    void hide() {
        Pane pane = filterPane();
        assertThat(pane).isVisible();
        controller.hide();
        assertThat(pane).isInvisible();
    }

    @Test
    @DisplayName("toggles the filterPanel")
    void toggle() {
        Pane pane = filterPane();
        assertThat(pane).isVisible();

        interact(controller::toggle);
        assertThat(pane).isInvisible();

        interact(controller::toggle);
        assertThat(pane).isVisible();
        assertThat(tagsInput()).isFocused();
    }

    @Test
    @DisplayName("updates the Property-Object when the tags changed")
    void tagsChanged() {
        tagsInput().setText("hello world");

        var filterProperties = controller.filterProperties().get();
        assertThat(filterProperties.getTags()).isNotEmpty();
        assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("hello", "world");
    }

    @Test
    @DisplayName("ignores duplicate tags")
    void ignoreDuplicateTags() {
        tagsInput().setText("abc def abc");

        var filterProperties = controller.filterProperties().get();
        assertThat(filterProperties.getTags()).isNotEmpty();
        assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("abc", "def");
    }

    @Test
    @DisplayName("updates the Property-Object without tags when the tag input is cleared")
    void emptyTagInput() {
        var inputField = tagsInput();

        inputField.setText("test");
        assertThat(controller.filterProperties().get().getTags()).isNotEmpty();

        inputField.setText("");
        assertThat(controller.filterProperties().get().getTags()).isEmpty();
    }

    @Test
    @DisplayName("updates the Property-Object when the programming language changed")
    void languageChanged() {
        var comboBox = languageSelection();
        assertThat(comboBox.getItems()).hasSize(5);
        assertThat(comboBox.getItems().getFirst()).isEqualTo(new LanguageItem(null, "All"));

        interact(() -> comboBox.getSelectionModel().select(3));

        var filterProperties = controller.filterProperties().get();
        assertThat(filterProperties.getLanguage()).isNotEmpty();
        assertThat(filterProperties.getLanguage().get()).isEqualTo(JAVA);
    }

    @Test
    @DisplayName("clears all filter when pressing the 'clearFilter' button")
    void clearFilter() {
        clickOn(clearFilterButton());

        assertThat(tagsInput().getText()).isEmpty();
        assertThat(languageSelection().getSelectionModel().getSelectedItem().language()).isNull();
        var filterProperties = controller.filterProperties().get();
        assertThat(filterProperties.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when receiving a QuickFilterEvent")
    class QuickFilterTest {

        @Test
        @DisplayName("applies the language")
        void quickfilterLanguage() {
            interact(() -> controller.addFilter(new QuickFilterEvent(JAVA)));
            assertThat(languageSelection().getSelectionModel().getSelectedItem().language()).isEqualTo(JAVA);

            var filterProperties = controller.filterProperties().get();
            assertThat(filterProperties.getLanguage()).isNotEmpty();
            assertThat(filterProperties.getLanguage().get()).isEqualTo(JAVA);
        }
        @Test
        @DisplayName("adds the nested tag")
        void quickfilterTag() {
            tagsInput().setText("tag1 tag2");
            controller.addFilter(new QuickFilterEvent("another-tag"));

            assertThat(tagsInput().getText()).isEqualTo("tag1 tag2 another-tag");
            var filterProperties = controller.filterProperties().get();
            assertThat(filterProperties.getTags()).isNotEmpty();
            assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("tag1", "tag2", "another-tag");
        }

        @Test
        @DisplayName("ignores duplicate tags")
        void ignoreDuplicateTags() {
            String text = "tag1 tag2 tag3";
            tagsInput().setText(text);

            controller.addFilter(new QuickFilterEvent("tag2"));

            assertThat(tagsInput().getText()).isEqualTo(text);
            var filterProperties = controller.filterProperties().get();
            assertThat(filterProperties.getTags()).isNotEmpty();
            assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("tag1", "tag2", "tag3");
        }
    }

    private Pane filterPane() {
        return lookup("#filterPanel").queryAs(Pane.class);
    }

    private TextInputControl tagsInput() {
        return lookup("#tagsInput").queryTextInputControl();
    }

    private ComboBox<LanguageItem> languageSelection() {
        return lookup("#languageSelection").queryComboBox();
    }

    private Button clearFilterButton() {
        return lookup("#clearFilter").queryButton();
    }
}