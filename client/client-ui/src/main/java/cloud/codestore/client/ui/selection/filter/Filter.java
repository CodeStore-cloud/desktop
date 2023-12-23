package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Set;

@FxController
public class Filter {
    private final EventBus eventBus;

    public Filter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @FXML
    private TextField tagsInput;

    @FXML
    private void initialize() {
        tagsInput.textProperty().addListener((field, oldValue, newValue) -> {
            Set<String> tags = Set.of(newValue.split(" "));
            FilterProperties filterProperties = new FilterProperties(tags);
            eventBus.post(new FilterEvent(filterProperties));
        });
    }
}
