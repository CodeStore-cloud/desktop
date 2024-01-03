package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.ui.FxController;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@FxController
public class SnippetDetails {
    @FXML
    private TextField tagsInput;

    public void bindEditing(BooleanProperty editingProperty) {
        tagsInput.editableProperty().bind(editingProperty);
    }

    public void setTags(@Nonnull List<String> tags) {
        tagsInput.setText(String.join(" ", tags));
    }

    public List<String> getTags() {
        String text = tagsInput.getText();
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split(" ")).map(String::trim).toList();
    }
}
