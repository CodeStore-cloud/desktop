package cloud.codestore.core.application.synchronization;

import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationConfiguration;
import cloud.codestore.core.usecases.synchronizesnippets.WriteSynchronizationConfigurationQuery;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The dialog that provides the selection of a cloud service to synchronize the code snippets.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CloudSelectionDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudSelectionDialog.class);
    private static final String FXML_FILE_NAME = "cloudSelectionDialog.fxml";

    private final WriteSynchronizationConfigurationQuery writeSynchronizationConfigurationQuery;
    private final SynchronizationExecutor synchronizationExecutor;
    private ResourceBundle resourceBundle;
    @FXML
    private ComboBox<CloudService> cloudServiceSelection;

    CloudSelectionDialog(
            @Nonnull WriteSynchronizationConfigurationQuery writeSynchronizationConfigurationQuery,
            @Nonnull SynchronizationExecutor synchronizationExecutor
    ) {
        this.writeSynchronizationConfigurationQuery = writeSynchronizationConfigurationQuery;
        this.synchronizationExecutor = synchronizationExecutor;
    }

    /**
     * Shows this dialog.
     */
    public void show() {
        URL fxmlFile = getClass().getResource(FXML_FILE_NAME);
        Objects.requireNonNull(fxmlFile, "Cannot find " + FXML_FILE_NAME);

        resourceBundle = ResourceBundle.getBundle("dialog-messages");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> this);

        Platform.runLater(() -> {
            try {
                Stage window = fxmlLoader.load();
                setAvailableCloudServices();
                window.show();
            } catch (IOException exception) {
                LOGGER.error("Failed to show cloud selection dialog.");
                // TODO show error dialog
            }
        });
    }

    @FXML
    void ok() {
        CloudService selectedCloudService = cloudServiceSelection.getSelectionModel().getSelectedItem();
        var synchronizationConfiguration = new SynchronizationConfiguration(selectedCloudService);
        writeSynchronizationConfigurationQuery.write(synchronizationConfiguration);
        synchronizationExecutor.synchronizeSnippets();
    }

    private void setAvailableCloudServices() {
        cloudServiceSelection.getItems().setAll(CloudService.values());
        cloudServiceSelection.getSelectionModel().select(CloudService.NONE);
        cloudServiceSelection.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CloudService cloudService, boolean empty) {
                super.updateItem(cloudService, empty);
                String text = empty || cloudService == null ? null : switch (cloudService) {
                    case NONE -> resourceBundle.getString("dialog.cloud.service.none");
                    case GOOGLE_DRIVE -> resourceBundle.getString("dialog.cloud.service.google");
                };

                setText(text);
            }
        });
    }
}
