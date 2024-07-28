package cloud.codestore.client.ui;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import java.net.URL;

public class FXMLLoaderFactory {
    private static Callback<Class<?>, Object> controllerFactory;

    public static void setControllerFactory(Callback<Class<?>, Object> controllerFactory) {
        FXMLLoaderFactory.controllerFactory = controllerFactory;
    }

    static FXMLLoader createFXMLLoader(URL fxmlFile) {
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile);
        fxmlLoader.setResources(UiMessages.bundle());
        fxmlLoader.setControllerFactory(controllerFactory);

        return fxmlLoader;
    }
}
