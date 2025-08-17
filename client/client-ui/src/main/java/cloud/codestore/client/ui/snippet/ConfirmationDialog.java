package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.ui.FXMLLoaderFactory;
import cloud.codestore.client.ui.UiMessages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Represents a modal dialog to confirm a specific action.
 */
public class ConfirmationDialog {
    @FXML
    private Label label;
    @FXML
    private Button yes;
    @FXML
    private Button no;
    @FXML
    private Button cancel;
    private Stage window;

    ConfirmationDialog(String titleKey, String messageKey) {
        try {
            window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.initStyle(StageStyle.UTILITY);

            FXMLLoader fxmlLoader = FXMLLoaderFactory.createFXMLLoader(getClass().getResource("confirmationDialog.fxml"));
            fxmlLoader.setControllerFactory(controllerClass -> this);
            window.setScene(new Scene(fxmlLoader.load()));

            window.setTitle(UiMessages.get(titleKey));
            label.setText(UiMessages.get(messageKey));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    ConfirmationDialog setCancellable(boolean cancellable) {
        cancel.managedProperty().bind(cancel.visibleProperty());
        cancel.setVisible(cancellable);
        cancel.setCancelButton(cancellable);
        no.setCancelButton(!cancellable);
        return this;
    }

    ConfirmationDialog onYes(Runnable action) {
        yes.setOnAction(event -> {
            close();
            action.run();
        });
        return this;
    }

    ConfirmationDialog onNo(Runnable action) {
        no.setOnAction(event -> {
            close();
            action.run();
        });
        return this;
    }

    void show() {
        window.show();
    }

    @FXML
    private void close() {
        window.close();
    }
}
