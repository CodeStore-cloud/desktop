package cloud.codestore.client.ui.snippet.title;

import cloud.codestore.client.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@FxController
public class SnippetTitle {
    @FXML
    private TextField snippetTitle;

    public void setText(String title) {
        snippetTitle.setText(title);
    }

    public String getText() {
        return snippetTitle.getText();
    }

    @FXML
    private void initialize() {
        snippetTitle.setEditable(false);
    }
}
