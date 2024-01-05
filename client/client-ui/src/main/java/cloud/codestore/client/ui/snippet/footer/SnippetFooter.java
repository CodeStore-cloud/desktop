package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javax.annotation.Nonnull;
import java.util.Set;

@FxController
public class SnippetFooter implements SnippetForm {
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @FXML
    private void initialize() {
        saveButton.managedProperty().bind(saveButton.visibleProperty());
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
    }

    @Override
    public void setEditable(boolean editable) {
        saveButton.setVisible(editable);
        cancelButton.setVisible(editable);
        deleteButton.setVisible(!editable);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        Set<Permission> permissions = snippet.getPermissions();
        deleteButton.setVisible(permissions.contains(Permission.DELETE));
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {}

    public void onSave(Runnable callback) {
        saveButton.setOnAction(event -> callback.run());
    }

    public void onCancel(Runnable callback) {
        cancelButton.setOnAction(event -> callback.run());
    }

    public void onDelete(Runnable callback) {
        deleteButton.setOnAction(event -> callback.run());
    }
}
