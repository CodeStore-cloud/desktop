package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@FxController
public class SnippetDetails implements SnippetForm {
    @FXML
    public VBox details;
    @FXML
    private TextField tagsInput;

    @Override
    public void setEditing(boolean editable) {
        tagsInput.setEditable(editable);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        String tagsString = String.join(" ", snippet.getTags());
        tagsInput.setText(tagsString);
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        String text = tagsInput.getText();
        List<String> tags = text.isEmpty() ? Collections.emptyList() : List.of(text.split(" "));
        builder.tags(tags);
    }
}
