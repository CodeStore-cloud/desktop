package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The filter button")
class ToggleFilterButtonTest extends AbstractUiTest {
    private static final String STYLE_FILLED = "filled";

    private EventBus eventBus = new EventBus();
    private Button filterButton;

    @Start
    public void start(Stage stage) throws Exception {
        ToggleFilterButton controller = new ToggleFilterButton(eventBus);
        start(stage, "toggleFilterButton.fxml", controller);
        filterButton = lookup("#filterButton").queryButton();
    }

    @Nested
    @DisplayName("after applying filters")
    class Filtered {
        @BeforeEach
        void setUp() {
            assertThat(filterButton.getStyleClass()).doesNotContain(STYLE_FILLED);
            eventBus.post(new FilterEvent(new FilterProperties(Set.of("Tag1"), null)));
        }

        @Test
        @DisplayName("is filled")
        void filled() {
            assertThat(filterButton.getStyleClass()).contains(STYLE_FILLED);
        }

        @Nested
        @DisplayName("after clearing all filters")
        class NoFilters {
            @BeforeEach
            void setUp() {
                assertThat(filterButton.getStyleClass()).contains(STYLE_FILLED);
                eventBus.post(new FilterEvent(new FilterProperties()));
            }

            @Test
            @DisplayName("is filled")
            void filled() {
                assertThat(filterButton.getStyleClass()).doesNotContain(STYLE_FILLED);
            }
        }
    }
}