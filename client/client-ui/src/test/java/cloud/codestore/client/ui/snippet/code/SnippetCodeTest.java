package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.QuickFilterEvent;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The code controller")
class SnippetCodeTest extends AbstractUiTest {
    @Spy
    private EventBus eventBus = new EventBus();
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
        controller = new SnippetCode(readLanguagesUseCase, eventBus);
        start(stage, "code.fxml", controller);
        // Editor is loaded synchronously in the tests, so no need to wait until it´s loaded
        eventBus.post(new ApplicationReadyEvent());
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
        var quickfilterButton = languageQuickfilterButton();

        interact(() -> controller.setEditing(true));
        assertBrowserIsEditable(true);
        assertThat(comboBox).isVisible();
        assertThat(quickfilterButton).isInvisible();

        interact(() -> controller.setEditing(false));
        assertBrowserIsEditable(false);
        assertThat(comboBox).isInvisible();
        assertThat(quickfilterButton).isVisible();
    }

    private void assertBrowserIsEditable(boolean expectedValue) {
        interact(() -> assertThat(browser().getEngine().executeScript("editor.isEditable();")).isEqualTo(expectedValue));
    }

    @Test
    @DisplayName("sets the code and language of the given snippet")
    void setDescription() {
        Snippet snippet = showTestSnippet();
        Language selectedLanguage = languageSelection().getSelectionModel().getSelectedItem();
        assertThat(selectedLanguage).isEqualTo(snippet.getLanguage());
        assertThat(languageQuickfilterButton().getText()).isEqualTo(snippet.getLanguage().name());

        interact(() -> {
            var content = browser().getEngine().executeScript("editor.getContent();");
            assertThat(content).isEqualTo(snippet.getCode());
        });
    }

    @Test
    @DisplayName("reads the code and language into the given snippet builder")
    void readDescription() {
        Snippet testSnippet = showTestSnippet();
        SnippetBuilder builder = Snippet.builder();
        interact(() -> controller.visit(builder));

        Snippet readSnippet = builder.build();
        assertThat(readSnippet.getCode()).isEqualTo(testSnippet.getCode());
        assertThat(readSnippet.getLanguage()).isEqualTo(testSnippet.getLanguage());
    }

    @Test
    @DisplayName("dynamically shows the language selection")
    void showHideLanguageDropdown() {
        showTestSnippet();
        var languageSelection = languageSelection();
        var quickfilterButton = languageQuickfilterButton();

        assertThat(languageSelection).isInvisible();
        assertThat(quickfilterButton).isVisible();

        interact(() -> controller.setEditing(true));
        assertThat(languageSelection).isVisible();
        assertThat(quickfilterButton).isInvisible();
    }

    @Test
    @DisplayName("fires a QuickFilterEvent when pressing the quickfilterLanguage button")
    void quickfilterLanguage() {
        AtomicReference<QuickFilterEvent> capturedEvent = new AtomicReference<>();
        languageQuickfilterButton().addEventFilter(QuickFilterEvent.ANY, capturedEvent::set);

        Snippet snippet = showTestSnippet();
        clickOn(languageQuickfilterButton());

        QuickFilterEvent event = capturedEvent.get();
        assertThat(event).isNotNull();
        assertThat(event.getLanguage()).isPresent().get().isEqualTo(snippet.getLanguage());
    }

    private Snippet showTestSnippet() {
        Snippet snippet = Snippet.builder()
                                 .code("print(\"Hello, World!\");")
                                 .language(new Language("Python", "1"))
                                 .build();

        interact(() -> {
            controller.setEditing(false);
            controller.visit(snippet);
        });
        return snippet;
    }

    private ComboBox<Language> languageSelection() {
        return lookup("#languageSelection").queryComboBox();
    }

    private Label languageQuickfilterButton() {
        return lookup("#languageQuickfilter").queryAs(Label.class);
    }

    private WebView browser() {
        return lookup("#browser").queryAs(WebView.class);
    }
}