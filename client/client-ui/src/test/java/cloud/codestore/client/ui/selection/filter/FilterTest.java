package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The filter controller")
class FilterTest extends AbstractUiTest {
    private static final Language JAVA = new Language("Java", "3");

    @Mock
    private ReadLanguagesUseCase readLanguagesUseCase;
    @Spy
    private EventBus eventBus;
    @InjectMocks
    private Filter controller;

    @Start
    public void start(Stage stage) throws Exception {
        when(readLanguagesUseCase.readLanguages()).thenReturn(List.of(
                new Language("Python", "1"),
                new Language("HTML", "2"),
                JAVA,
                new Language("Kotlin", "4")
        ));

        start(stage, "filter.fxml", controller);
        eventBus.post(new ApplicationReadyEvent());
        eventBus.post(new ToggleFilterEvent());
        clearInvocations(eventBus);
    }

    @Test
    @DisplayName("triggers a FilterEvent when the tags changed")
    void tagsChanged() {
        tagsInput().setText("hello world");

        var argument = ArgumentCaptor.forClass(FilterEvent.class);
        verify(eventBus).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties();
        assertThat(filterProperties.getTags()).isNotEmpty();
        assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("hello", "world");
    }

    @Test
    @DisplayName("ignores duplicate tags")
    void ignoreDuplicateTags() {
        tagsInput().setText("abc def abc");

        var argument = ArgumentCaptor.forClass(FilterEvent.class);
        verify(eventBus).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties();
        assertThat(filterProperties.getTags()).isNotEmpty();
        assertThat(filterProperties.getTags().get()).containsExactlyInAnyOrder("abc", "def");
    }

    @Test
    @DisplayName("triggers a FilterEvent without tags when the tag input is empty")
    void emptyTagInput() {
        TextInputControl inputField = tagsInput();
        inputField.setText("test");
        inputField.setText("");

        var argument = ArgumentCaptor.forClass(FilterEvent.class);
        verify(eventBus, times(2)).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties(); // second call
        assertThat(filterProperties.getTags()).isEmpty();
    }

    @Test
    @DisplayName("triggers a FilterEvent when the programming language changed")
    void languageChanged() {
        var comboBox = languageSelection();
        assertThat(comboBox.getItems()).hasSize(5);
        assertThat(comboBox.getItems().getFirst()).isEqualTo(new LanguageItem(null, "All"));

        interact(() -> comboBox.getSelectionModel().select(3));

        var argument = ArgumentCaptor.forClass(FilterEvent.class);
        verify(eventBus).post(argument.capture());
        var filterProperties = argument.getValue().filterProperties();
        assertThat(filterProperties.getLanguage()).isNotEmpty();
        assertThat(filterProperties.getLanguage().get()).isEqualTo(JAVA);
    }

    @Test
    @DisplayName("clears all filter when pressing the 'clearFilter' button")
    void clearFilter() {
        clearInvocations(eventBus);
        clickOn(clearFilterButton());

        assertThat(tagsInput().getText()).isEmpty();
        assertThat(languageSelection().getSelectionModel().getSelectedItem().language()).isNull();
        var argument = ArgumentCaptor.forClass(FilterEvent.class);
        verify(eventBus).post(argument.capture());
        assertThat(argument.getValue().filterProperties().isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when recieving a QuickFilterEvent")
    class QuickFilterTest {
        @Test
        @DisplayName("applies the language")
        void quickfilterLanguage() {
            interact(() -> eventBus.post(new QuickFilterEvent(JAVA)));
            assertThat(languageSelection().getSelectionModel().getSelectedItem().language()).isEqualTo(JAVA);

            var argument = ArgumentCaptor.forClass(FilterEvent.class);
            verify(eventBus).post(argument.capture());
            var filterProperties = argument.getValue().filterProperties();
            assertThat(filterProperties.getLanguage()).isNotEmpty();
            assertThat(filterProperties.getLanguage().get()).isEqualTo(JAVA);
        }

        @Test
        @DisplayName("adds the nested tag")
        void quickfilterTag() {
            tagsInput().setText("tag1 tag2");
            interact(() -> eventBus.post(new QuickFilterEvent("another-tag")));
            assertThat(tagsInput().getText()).isEqualTo("tag1 tag2 another-tag");
        }

        @Test
        @DisplayName("ignores duplicate tags")
        void ignoreDuplicateTags() {
            String text = "tag1 tag2 tag3";
            tagsInput().setText(text);
            eventBus.post(new QuickFilterEvent("tag2"));
            assertThat(tagsInput().getText()).isEqualTo(text);
        }
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