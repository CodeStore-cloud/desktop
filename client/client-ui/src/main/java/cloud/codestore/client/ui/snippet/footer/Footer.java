package cloud.codestore.client.ui.snippet.footer;

import cloud.codestore.client.Permission;
import cloud.codestore.client.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javax.annotation.Nonnull;
import java.util.Set;

@FxController
public class Footer {
    @FXML
    private Button deleteButton;

    @FXML
    private void initialize() {
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
    }

    public void onDelete(Runnable runnable) {
        deleteButton.setOnAction(event -> runnable.run());
    }

    public void setPermissions(@Nonnull Set<Permission> permissions) {
        deleteButton.setVisible(permissions.contains(Permission.DELETE));
    }
}
