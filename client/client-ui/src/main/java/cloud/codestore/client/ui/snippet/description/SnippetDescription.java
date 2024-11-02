package cloud.codestore.client.ui.snippet.description;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextBoundsType;

import javax.annotation.Nonnull;

@FxController
public class SnippetDescription implements SnippetForm {
    @FXML
    private TextArea snippetDescription;

    @FXML
    void initialize() {
        snippetDescription.minHeightProperty().bind(snippetDescription.prefHeightProperty());
    }

    @Override
    public void setEditing(boolean editable) {
        snippetDescription.setEditable(editable);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        snippetDescription.setText(snippet.getDescription());
        updateHeight();
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        builder.description(snippetDescription.getText());
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
