package cloud.codestore.client.ui;

import javafx.application.HostServices;

import javax.annotation.Nonnull;

/**
 * Represents the system's default browser.
 */
public class DefaultBrowser {
    private static HostServices hostServices;

    static void init(HostServices hostServices) {
        DefaultBrowser.hostServices = hostServices;
    }

    /**
     * Opens the given URL in the default browser.
     * @param url the URL to visit. Must not be {@code null}.
     */
    public static void visit(@Nonnull String url) {
        hostServices.showDocument(url);
    }
}
