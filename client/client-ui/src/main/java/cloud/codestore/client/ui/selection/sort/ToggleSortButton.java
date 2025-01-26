package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;

@FxController
public class ToggleSortButton {
    private EventBus eventBus;

    ToggleSortButton(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @FXML
    void toggleSort() {
        eventBus.post(new ToggleSortEvent());
    }
}
