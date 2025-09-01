package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.FxController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@FxController
public class FullTextSearch {
    @FXML
    private TextField inputField;
    private final Map<KeyCode, Runnable> keyCodeHandlers = new HashMap<>();

    @FXML
    public void initialize() {
        inputField.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.ESCAPE) {
                clearInput();
            } else if (keyCodeHandlers.containsKey(keyCode)) {
                keyCodeHandlers.get(keyCode).run();
            }
        });
    }

    /**
     * @return a {@link StringProperty} to read the content of the input field.
     */
    public StringProperty inputProperty() {
        var inputProperty = new SimpleStringProperty();
        inputProperty.bind(inputField.textProperty());
        return inputProperty;
    }

    /**
     * Registers a function to be called whenever the specified key is pressed.
     * @param keyCode the key-code of a key.
     * @param runnable the function to be called.
     */
    public void registerKeyHandler(@Nonnull KeyCode keyCode, @Nonnull Runnable runnable) {
        keyCodeHandlers.put(keyCode, runnable);
    }

    @FXML
    private void clearInput() {
        inputField.clear();
    }
}
