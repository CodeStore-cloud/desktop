package cloud.codestore.core.application.update;

import cloud.codestore.core.application.CodeStoreSystemTray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Checks for updates and shows corresponding messages and dialogs.
 */
@Component
class Updater {

    private final LatestApplication latestApplication;
    private final String currentVersion;
    private final CodeStoreSystemTray tray;

    @Autowired
    Updater(
            @Value("${application.version}") String currentVersion,
            LatestApplication latestApplication,
            CodeStoreSystemTray tray
    ) {
        this.latestApplication = latestApplication;
        this.currentVersion = currentVersion;
        this.tray = tray;
    }

    @EventListener(ApplicationReadyEvent.class)
    void checkForUpdates() {
        latestApplication.isNewerThan(currentVersion)
                         .thenAccept(this::updateSystemTrayMenu);
    }

    private void updateSystemTrayMenu(boolean updateAvailable) {
        if (updateAvailable) {
            tray.showUpdateMessage();
            tray.setUpdateHandler(this::downloadUpdate);
        }
    }

    private void downloadUpdate(ActionEvent event) {
        UpdateDialog dialog = UpdateDialog.show();

        try {
            InstallerExecutable installer = latestApplication.getInstaller();
            dialog.onCancel(installer::cancelDownload);
            installer.setProgressListener(dialog::setProgress);
            installer.download();
            dialog.close();
            installer.execute();
            //TODO exit application
        } catch (IOException exception) {
            // TODO show error dialog
            throw new RuntimeException(exception);
        }
    }
}
