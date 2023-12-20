package cloud.codestore.client.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AbstractUiTest {
    protected void start(Stage stage, String fxmlFileName, Object controller) throws Exception {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("uiMessages");
        URL fxmlFile = getClass().getResource(fxmlFileName);
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile, resourceBundle);
        fxmlLoader.setControllerFactory(controllerClass -> controller);
        Parent parent = fxmlLoader.load();

        stage.setScene(new Scene(parent));
        stage.show();
    }
}
