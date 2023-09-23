package cloud.codestore.client.ui.snippet.description;

import cloud.codestore.client.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

@FxController
public class SnippetDescription {
    @FXML
    private TextArea snippetDescription;

    public String getText() {
        return snippetDescription.getText();
    }

    public void setText(String description) {
        snippetDescription.setText(description);
    }

    @FXML
    private void initialize() {
        snippetDescription.setEditable(false);
    }
}
