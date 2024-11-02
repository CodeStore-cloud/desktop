package cloud.codestore.client.ui.footer;

import cloud.codestore.client.ui.DefaultBrowser;
import cloud.codestore.client.ui.FxController;
import javafx.fxml.FXML;

/**
 * The controller of the footer of the main window.
 */
@FxController
public class Footer {
    /**
     * Shows the {CodeStore} imprint in the default browser.
     */
    @FXML
    public void showImprint() {
        DefaultBrowser.visit("https://codestore.cloud/imprint");
    }

    /**
     * Shows the {CodeStore} privacy policy in the default browser.
     */
    @FXML
    public void showPrivacy() {
        DefaultBrowser.visit("https://codestore.cloud/privacy");
    }
}
