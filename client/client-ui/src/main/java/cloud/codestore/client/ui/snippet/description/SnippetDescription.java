package cloud.codestore.client.ui.snippet.description;

import cloud.codestore.client.ui.FxController;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextBoundsType;

@FxController
public class SnippetDescription {
    @FXML
    private TextArea snippetDescription;

    @FXML
    private void initialize() {
        snippetDescription.setEditable(false);
    }

    public String getText() {
        return snippetDescription.getText();
    }

    public void setText(String description) {
        snippetDescription.setText(description);
        updateHeight();
    }

    public void bindEditing(BooleanProperty editingProperty) {
        snippetDescription.editableProperty().bind(editingProperty);
    }

    private void updateHeight() {
        double height = Utils.computeTextHeight(
                snippetDescription.getFont(),
                snippetDescription.getText(),
                snippetDescription.getWidth(),
                TextBoundsType.VISUAL
        );

        snippetDescription.setPrefHeight(height + 10);
    }
}
