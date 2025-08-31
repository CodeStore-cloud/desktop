package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.ui.AbstractUiTest;
import cloud.codestore.client.usecases.listsnippets.SortProperties;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.Start;

import java.util.stream.Stream;

import static cloud.codestore.client.usecases.listsnippets.SortProperties.SnippetProperty.*;
import static org.testfx.assertions.api.Assertions.assertThat;

@DisplayName("The sort controller")
class SortTest extends AbstractUiTest {
    private Sort controller = new Sort();
    private ComboBox<SortItem> comboBox;

    @Start
    public void start(Stage stage) throws Exception {
        start(stage, "sort.fxml", controller);
        comboBox = lookup("#sortSelection").queryComboBox();
        controller.toggle();
    }

    @Test
    @DisplayName("selects default option on initialization")
    void defaultSelection() {
        var defaultProperty = new SortProperties().property();
        assertSelected(defaultProperty);
    }

    @Test
    @DisplayName("hides the sortPanel")
    void hide() {
        Pane pane = sortPane();
        assertThat(pane).isVisible();
        controller.hide();
        assertThat(pane).isInvisible();
    }

    @Test
    @DisplayName("toggles the sortPanel")
    void toggle() {
        Pane pane = sortPane();
        assertThat(pane).isVisible();
        controller.toggle();
        assertThat(pane).isInvisible();
        controller.toggle();
        assertThat(pane).isVisible();
    }

    @ParameterizedTest
    @MethodSource("sortProperties")
    @DisplayName("updates the Property-Object when the sort selection was changed")
    void updatePropertyOnSelection(SortProperties expectedSortProperties) {
        interact(() -> select(expectedSortProperties.property()));
        assertThat(controller.sortProperties().get()).isEqualTo(expectedSortProperties);
    }

    @ParameterizedTest
    @MethodSource("sortProperties")
    @DisplayName("updates the dropdown menu when the Property-Object is changed from outside")
    void updateSelection(SortProperties value) {
        interact(() -> controller.sortProperties().set(value));
        assertSelected(value.property());
    }

    private static Stream<Arguments> sortProperties() {
        return Stream.of(
                Arguments.of(new SortProperties(RELEVANCE, true)),
                Arguments.of(new SortProperties(TITLE, true)),
                Arguments.of(new SortProperties(CREATED, false)),
                Arguments.of(new SortProperties(MODIFIED, false))
        );
    }

    private void select(SortProperties.SnippetProperty property) {
        for (SortItem item : comboBox.getItems()) {
            if (item.property() == property) {
                comboBox.getSelectionModel().select(item);
                break;
            }
        }
    }

    private void assertSelected(SortProperties.SnippetProperty property) {
        SortItem selectedItem = comboBox.getSelectionModel().getSelectedItem();
        assertThat(selectedItem.property()).isEqualTo(property);
    }

    private Pane sortPane() {
        return lookup("#sortPanel").queryAs(Pane.class);
    }
}