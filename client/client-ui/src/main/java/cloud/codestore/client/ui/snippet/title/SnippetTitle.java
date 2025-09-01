package cloud.codestore.client.ui.snippet.title;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javax.annotation.Nonnull;

@FxController
public class SnippetTitle implements SnippetForm {
    @FXML
    private TextField snippetTitle;

    @Override
    public void setEditing(boolean editing) {
        snippetTitle.setEditable(editing);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        snippetTitle.setText(snippet.getTitle());
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        builder.title(snippetTitle.getText());
    }
}
