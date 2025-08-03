package cloud.codestore.core.application.update;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class to start the JavaFX runtime.
 */
class JavaFxInitializer {
    private static final AtomicBoolean javaFxRuntimeStarted = new AtomicBoolean(false);

    static void startJavaFxRuntime() {
        if (javaFxRuntimeStarted.compareAndSet(false, true)) {
            Platform.startup(() -> {});
        }
    }
}
