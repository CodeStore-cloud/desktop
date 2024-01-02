package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.ui.FxController;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

@FxController
public class SnippetCode {
    @FXML
    private TextArea snippetCode;

    public String getText() {
        return snippetCode.getText();
    }

    public void setText(String description) {
        snippetCode.setText(description);
    }

    public void bindEditing(BooleanProperty editingProperty) {
        snippetCode.editableProperty().bind(editingProperty);
    }

    @FXML
    private void initialize() {
        snippetCode.setEditable(false);
    }
}
