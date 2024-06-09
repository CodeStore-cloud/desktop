package cloud.codestore.client.ui.history;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.ui.selection.ClearSelectionEvent;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.SnippetDeletedEvent;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.Start;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("The history")
class HistoryTest extends AbstractUiTest {
    @Spy
    private EventBus eventBus = new EventBus();

    @Start
    public void start(Stage stage) throws Exception {
        History controller = new History(eventBus);
        start(stage, "history.fxml", controller);
    }

    @Nested
    @DisplayName("when empty")
    class EmptyHistory {
        @Test
        @DisplayName("disables both buttons")
        void buttonsDisabled() {
            assertThat(prevSnippetButton()).isDisabled();
            assertThat(nextSnippetButton()).isDisabled();
        }
    }

    @Nested
    @DisplayName("when containing one snippet")
    class HistoryContainingOneSnippet {
        @BeforeEach
        void setUp() {
            eventBus.post(new SnippetSelectedEvent("1"));
        }

        @Test
        @DisplayName("disables both buttons")
        void disableButtons() {
            assertThat(prevSnippetButton()).isDisabled();
            assertThat(nextSnippetButton()).isDisabled();
        }

        @Nested
        @DisplayName("when deleting the snippet")
        class DeleteSnippet {
            @Test
            @DisplayName("throws a ClearSelectionEvent")
            void clearSelection() {
                reset(eventBus);
                eventBus.post(new SnippetDeletedEvent("1"));
                verify(eventBus).post(new ClearSelectionEvent());
            }
        }
    }

    @Nested
    @DisplayName("when containing multiple snippets")
    class HistoryContainingMultipleSnippets {
        @BeforeEach
        void setUp() {
            eventBus.post(new SnippetSelectedEvent("1"));
            eventBus.post(new SnippetSelectedEvent("2"));
        }

        @Test
        @DisplayName("enables the \"previous\" button")
        void enablePrevButton() {
            assertThat(prevSnippetButton()).isEnabled();
            assertThat(nextSnippetButton()).isDisabled();
        }

        @Nested
        @DisplayName("going back in the history")
        class HistoryBack {
            @BeforeEach
            void setUp() {
                reset(eventBus);
                clickOn(prevSnippetButton());
            }

            @Test
            @DisplayName("selects the previous snippet")
            void selectPreviousSnippet() {
                verify(eventBus).post(new SnippetSelectedEvent("1"));
            }

            @Test
            @DisplayName("disables the \"previous\" button")
            void disablePrevButton() {
                assertThat(prevSnippetButton()).isDisabled();
            }

            @Test
            @DisplayName("enables the \"next\" button")
            void enableNextButton() {
                assertThat(nextSnippetButton()).isEnabled();
            }
        }

        @Nested
        @DisplayName("going forward in the history")
        class HistoryForward {
            @BeforeEach
            void setUp() {
                clickOn(prevSnippetButton());
                reset(eventBus);
                clickOn(nextSnippetButton());
            }

            @Test
            @DisplayName("selects the next code snippet")
            void selectSnippet() {
                verify(eventBus).post(new SnippetSelectedEvent("2"));
            }

            @Test
            @DisplayName("disables the \"next\" button")
            void disablePrevButton() {
                assertThat(nextSnippetButton()).isDisabled();
            }

            @Test
            @DisplayName("enables the \"previous\" button")
            void enableNextButton() {
                assertThat(prevSnippetButton()).isEnabled();
            }
        }
    }

    @Nested
    @DisplayName("when deleting the current snippet")
    class DeleteSnippet {
        @BeforeEach
        void setUp() {
            eventBus.post(new SnippetSelectedEvent("1"));
            eventBus.post(new SnippetSelectedEvent("2")); // current snippet
            eventBus.post(new SnippetSelectedEvent("3"));
            clickOn(prevSnippetButton());
        }

        @Test
        @DisplayName("selects the previous snippet if available")
        void deleteSnippetAndSelectPrevious() {
            assertThat(prevSnippetButton()).isEnabled();
            assertThat(nextSnippetButton()).isEnabled();
            reset(eventBus);

            eventBus.post(new SnippetDeletedEvent("2"));

            assertThat(prevSnippetButton()).isDisabled();
            assertThat(nextSnippetButton()).isEnabled();
            verify(eventBus).post(new SnippetSelectedEvent("1"));
        }

        @Test
        @DisplayName("selects the next snippet if no previous snippet is available")
        void deleteSnippetAndSelectNext() {
            clickOn(prevSnippetButton());
            assertThat(prevSnippetButton()).isDisabled();
            assertThat(nextSnippetButton()).isEnabled();
            reset(eventBus);

            eventBus.post(new SnippetDeletedEvent("1"));

            assertThat(prevSnippetButton()).isDisabled();
            assertThat(nextSnippetButton()).isEnabled();
            verify(eventBus).post(new SnippetSelectedEvent("2"));
        }
    }

    private Button prevSnippetButton() {
        return getButton("#prevSnippetButton");
    }

    private Button nextSnippetButton() {
        return getButton("#nextSnippetButton");
    }

    private Button getButton(String buttonId) {
        var button = lookup(buttonId).queryButton();
        button.setPrefSize(30, 30);
        return button;
    }
}