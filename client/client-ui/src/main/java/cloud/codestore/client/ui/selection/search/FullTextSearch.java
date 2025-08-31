package cloud.codestore.client.ui.selection.search;

import cloud.codestore.client.ui.FxController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.Objects;

@FxController
public class FullTextSearch {
    @FXML
    private TextField inputField;

    @FXML
    public void initialize() {
        inputField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
                clearInput();
                //                case TAB -> showFilter();
                //                case DOWN -> selectNext();
                //                case UP -> selectPrevious();
            }
        });
    }

    public StringProperty inputProperty() {
        var inputProperty = new SimpleStringProperty();
        inputProperty.bind(inputField.textProperty());
        return inputProperty;
    }

    @FXML
    private void clearInput() {
        inputField.clear();
    }
}
