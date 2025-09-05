package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.ui.ChangeSnippetsEvent;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.annotation.Nonnull;
import java.util.Collections;
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
    private Button editButton;

    private Set<Permission> permissions = Collections.emptySet();

    @FXML
    private void initialize() {
        saveButton.managedProperty().bind(saveButton.visibleProperty());
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        editButton.managedProperty().bind(editButton.visibleProperty());
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());

        saveButton.setOnAction(event -> saveButton.fireEvent(new ControlEvent(ControlEvent.SAVE)));
        cancelButton.setOnAction(event -> cancelButton.fireEvent(new ControlEvent(ControlEvent.CANCEL)));
        editButton.setOnAction(event -> editButton.fireEvent(
                new ChangeSnippetsEvent(ChangeSnippetsEvent.UPDATE_SNIPPET))
        );
        deleteButton.setOnAction(event -> editButton.fireEvent(
                new ChangeSnippetsEvent(ChangeSnippetsEvent.DELETE_SNIPPET))
        );

        saveButton.sceneProperty().addListener((observable, oldValue, scene) -> {
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), saveButton::fire);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), cancelButton::fire);
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), () -> {
                if (editButton.isVisible()) {
                    editButton.fire();
                }
            });
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
                if (deleteButton.isVisible()) {
                    deleteButton.fire();
                }
            });
        });
    }

    @Override
    public void setEditing(boolean editing) {
        saveButton.setVisible(editing);
        cancelButton.setVisible(editing);
        editButton.setVisible(!editing && permissions.contains(Permission.UPDATE));
        deleteButton.setVisible(!editing && permissions.contains(Permission.DELETE));
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        permissions = snippet.getPermissions();
    }
}
