package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javax.annotation.Nonnull;

@FxController
public class ToggleFilterButton {
    private static final String STYLE_FILLED = "filled";

    @FXML
    private Button filterButton;
    private EventBus eventBus;

    ToggleFilterButton(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @FXML
    void toggleFilter() {
        eventBus.post(new ToggleFilterEvent());
    }

    @Subscribe
    private void filter(@Nonnull FilterEvent event) {
        ObservableList<String> classes = filterButton.getStyleClass();
        if (event.filterProperties().isEmpty()) {
            classes.remove(STYLE_FILLED);
        } else if (!classes.contains(STYLE_FILLED)) {
            classes.add(STYLE_FILLED);
        }
    }
}
