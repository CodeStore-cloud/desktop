package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.Injectable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injectable
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
                event.consume();
            } else if (keyCodeHandlers.containsKey(keyCode)) {
                keyCodeHandlers.get(keyCode).run();
            }
        });

        inputField.sceneProperty()
                  .addListener((observable, oldValue, scene) ->
                          scene.getAccelerators().put(
                                  new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                                  this::focus
                          ));
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

    private void focus() {
        inputField.requestFocus();
    }

    @FXML
    private void clearInput() {
        inputField.clear();
    }
}
