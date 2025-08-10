package cloud.codestore.core.application.update;

import cloud.codestore.core.application.CodeStoreSystemTray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@DisplayName("The updater")
@ExtendWith(MockitoExtension.class)
class UpdaterTest {

    @Mock
    private LatestApplication latestApplication;
    @Mock
    private CodeStoreSystemTray tray;
    private Updater updater;

    @BeforeEach
    void setUp() {
        updater = new Updater("1.0", latestApplication, tray);
        when(latestApplication.isNewerThan(anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));
    }

    @Nested
    @DisplayName("when checking for updates")
    class CheckForUpdatesTest {
        @Test
        @DisplayName("shows update message in system tray if update is available")
        void showsUpdateMessageIfUpdateAvailable() {
            updater.checkForUpdates();

            verify(tray).showUpdateMessage();
            verify(tray).setUpdateHandler(any());
        }

        @Test
        @DisplayName("does not show update message if no update is available")
        void doesNotShowUpdateMessageIfNoUpdateAvailable() {
            when(latestApplication.isNewerThan(anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));

            updater.checkForUpdates();

            verify(tray, never()).showUpdateMessage();
            verify(tray, never()).setUpdateHandler(any());
        }
    }

    @Nested
    @DisplayName("when downloading installer")
    class DownloadInstallerTest {
        @Mock
        private InstallerExecutable installer;

        @Test
        @DisplayName("shows update dialog and downloads installer")
        void showsDialogAndDownloadsInstaller() throws Exception {
            try (MockedConstruction<UpdateDialog> mockDialog = Mockito.mockConstruction(UpdateDialog.class)) {
                when(latestApplication.getInstaller()).thenReturn(installer);

                performUpdate();

                UpdateDialog updateDialog = mockDialog.constructed().get(0);
                verify(updateDialog).onCancel(any());
                verify(installer).setProgressListener(any());
                verify(installer).download();
            }
        }
    }

    @Nested
    @DisplayName("after downloading finished")
    class AfterDownloadTest {
        private UpdateDialog updateDialog;
        @Mock
        private InstallerExecutable installer;

        @BeforeEach
        void setUp() throws Exception {
            try (MockedConstruction<UpdateDialog> mockDialog = Mockito.mockConstruction(UpdateDialog.class)) {
                when(latestApplication.getInstaller()).thenReturn(installer);
                performUpdate();
                updateDialog = mockDialog.constructed().get(0);
            }
        }

        @Test
        @DisplayName("close the update dialog")
        void closeDialog() {
            verify(updateDialog).close();
        }

        @Test
        @DisplayName("execute the installer")
        void executeInstaller() throws IOException {
            verify(installer).execute();
        }
    }

    private void performUpdate() {
        final ActionListener[] updateHandler = {null};

        doAnswer(invocation -> {
            updateHandler[0] = invocation.getArgument(0, ActionListener.class);
            return null;
        }).when(tray).setUpdateHandler(any());

        updater.checkForUpdates();
        try (MockedStatic<JavaFxInitializer> initializerMock = Mockito.mockStatic(JavaFxInitializer.class)) {
            updateHandler[0].actionPerformed(null);
        }
    }
}
