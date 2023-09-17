package cloud.codestore.client.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

/**
 * The JavaFX main class to show the GUI.
 */
public class FxApplication extends Application {
    @Override
    public void start(Stage window) {
        window.setTitle("{CodeStore}");
        setCodeStoreIcon(window);
        showMainWindow(window);
        hideLoadingScreen();
        DefaultBrowser.init(getHostServices());
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

    private void hideLoadingScreen() {
        Optional.ofNullable(SplashScreen.getSplashScreen())
                .ifPresent(SplashScreen::close);
    }
}
