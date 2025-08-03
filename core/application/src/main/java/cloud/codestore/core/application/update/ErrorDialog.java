package cloud.codestore.core.application.update;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class ErrorDialog extends Application {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("dialog-messages");
    private static final int PADDING = 10;
    private static ErrorDialog INSTANCE;

    private Stage window;
    private Exception exception;

    static ErrorDialog show(Exception exception) {
        Thread thread = new Thread(Application::launch);
        thread.setDaemon(true);
        thread.start();

        waitForDialogToBeVisible();
        INSTANCE.exception = exception;
        return INSTANCE;
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage window) throws Exception {
        this.window = window;
        window.setTitle(MESSAGES.getString("dialog.error.title"));

        Pane rootPane = createRootPane();
        Scene scene = new Scene(rootPane, rootPane.getPrefWidth(), rootPane.getPrefHeight());
        window.setScene(scene);
        window.setResizable(false);
        window.show();
    }

    private Pane createRootPane() {
        VBox pane = new VBox();
        pane.setPadding(new Insets(PADDING));
        pane.setSpacing(PADDING);

        TextFlow generalErrorMessage = new TextFlow();
        Hyperlink link = new Hyperlink(MESSAGES.getString("dialog.error.message.homepage"));
        link.setFocusTraversable(false);
        link.setOnAction(event -> getHostServices().showDocument("https://codestore.cloud"));
        generalErrorMessage.getChildren().addAll(
                new Text(MESSAGES.getString("dialog.error.message.1")),
                link,
                new Text(MESSAGES.getString("dialog.error.message.2"))
        );

        ButtonBar buttonBar = new ButtonBar();
        Button reportButton = new Button(MESSAGES.getString("dialog.error.report"));
        reportButton.setOnAction(event -> {
            reportError();
            window.close();
        });
        buttonBar.getButtons().addAll(reportButton);

        pane.getChildren().addAll(
                generalErrorMessage,
                buttonBar
        );

        return pane;
    }

    private void reportError() {
        // TODO send error report
    }

    private static void waitForDialogToBeVisible() {
        while (INSTANCE == null || !INSTANCE.window.isShowing()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exception);
            }
        }
    }
}
