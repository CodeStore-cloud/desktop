package cloud.codestore.client.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The JavaFX main class to show the GUI.
 */
public class FxApplication extends Application {
    private static final Logger LOGGER = LogManager.getLogger(FxApplication.class);
    private static CompletableFuture<Void> uiInitialized;

    @Override
    public void start(Stage window) {
        window.setTitle("{CodeStore} " + getVersion());
        setCodeStoreIcon(window);
        showMainWindow(window);
        hideLoadingScreen();
        DefaultBrowser.init(getHostServices());
        logStartTime();
        uiInitialized.complete(null);
    }

    public static void setUiInitializedCallback(@Nonnull CompletableFuture<Void> callback) {
        uiInitialized = callback;
    }

    private void setCodeStoreIcon(Stage window) {
        Optional.ofNullable(getClass().getResourceAsStream("icon.png"))
                .ifPresent(iconStream -> window.getIcons().add(new Image(iconStream)));
    }

    private void showMainWindow(Stage window) {
        try {
            FXMLLoader fxmlLoader = FXMLLoaderFactory.createFXMLLoader(getClass().getResource("root.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            window.setScene(scene);
            window.show();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO show error dialog
            Platform.exit();
        }
    }

    private String getVersion() {
        return new VersionReader().readVersion();
    }

    private void hideLoadingScreen() {
        Optional.ofNullable(SplashScreen.getSplashScreen())
                .ifPresent(SplashScreen::close);
    }

    private void logStartTime() {
        long jvmStart = ManagementFactory.getRuntimeMXBean().getStartTime();
        LOGGER.info("UI started in {}ms", System.currentTimeMillis() - jvmStart);
    }
}
