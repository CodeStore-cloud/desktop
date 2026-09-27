package cloud.codestore.core.application.synchronization;

import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationConfiguration;
import cloud.codestore.core.usecases.synchronizesnippets.WriteSynchronizationConfigurationQuery;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The cloud selection dialog")
class CloudSelectionDialogTest extends ApplicationTest {
    @Mock
    private WriteSynchronizationConfigurationQuery writeSynchronizationConfigurationQuery;
    @Mock
    private SynchronizationExecutor synchronizationExecutor;

    @Start
    public void start(Stage stage) {
        new CloudSelectionDialog(writeSynchronizationConfigurationQuery, synchronizationExecutor).show();
    }

    @Test
    @DisplayName("shows all available cloud services")
    void showSelection() {
        ComboBox<CloudService> comboBox = getComboBox();
        assertThat(comboBox.getItems()).containsAll(Arrays.asList(CloudService.values()));
        assertThat(comboBox.getSelectionModel().getSelectedItem()).isSameAs(CloudService.NONE);
    }

    @Nested
    @DisplayName("after selecting a cloud service")
    class CloudServiceSelected {
        @Test
        @DisplayName("saves the selected service in a configuration file")
        void saveSynchronizationConfiguration() {
            interact(() -> getComboBox().getSelectionModel().select(CloudService.GOOGLE_DRIVE));
            clickOn(".button");
            verify(writeSynchronizationConfigurationQuery).write(
                    argThat((SynchronizationConfiguration config) -> config.cloudService() == CloudService.GOOGLE_DRIVE)
            );
        }

        @Test
        @DisplayName("executes the synchronization")
        void executeSynchronization() {
            interact(() -> getComboBox().getSelectionModel().select(CloudService.GOOGLE_DRIVE));
            clickOn(".button");
            verify(synchronizationExecutor).synchronizeSnippets();
        }
    }

    private ComboBox<CloudService> getComboBox() {
        return lookup("#cloudServiceSelection").query();
    }
}