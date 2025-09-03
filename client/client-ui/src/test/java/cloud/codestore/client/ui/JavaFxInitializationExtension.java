package cloud.codestore.client.ui;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JavaFxInitializationExtension implements BeforeAllCallback {
    private static boolean javaFxInitialized;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!javaFxInitialized) {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        }
    }
}
