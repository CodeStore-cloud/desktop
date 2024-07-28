package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.Objects;

@FxController
public class FullTextSearch {
    private final EventBus eventBus;

    @FXML
    private TextField inputField;

    FullTextSearch(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @FXML
    public void initialize() {
        inputField.textProperty().addListener((textField, oldValue, newValue) -> search(newValue));
        inputField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
                clearInput();
                //                case TAB -> showFilter();
                //                case DOWN -> selectNext();
                //                case UP -> selectPrevious();
            }
        });
    }

    @FXML
    void clearInput() {
        inputField.clear();
    }

    private void search(String input) {
        eventBus.post(new FullTextSearchEvent(input));
    }
}
