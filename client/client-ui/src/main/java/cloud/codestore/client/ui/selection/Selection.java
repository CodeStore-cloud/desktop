package cloud.codestore.client.ui.selection;

import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;

@FxController
public class Selection {
    private EventBus eventBus;

    public Selection(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @FXML
    void toggleFilter() {
        eventBus.post(new ToggleFilterEvent());
    }

    @FXML
    void toggleSort() {
        eventBus.post(new ToggleSortEvent());
    }
}
