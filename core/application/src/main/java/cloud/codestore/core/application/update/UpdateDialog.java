package cloud.codestore.core.application.update;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

/**
 * The dialog that shows the progress of downloading the application.
 */
class UpdateDialog extends Application {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("dialog-messages");
    private static final int PADDING = 10;
    private static UpdateDialog INSTANCE;

    private Runnable cancelCallback = () -> {};
    private ProgressBar progressBar;
    private Stage stage;

    static UpdateDialog show() {
        launch(UpdateDialog.class);
        return INSTANCE;
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        if (stage.getStyle() != StageStyle.UNDECORATED) {
            stage.initStyle(StageStyle.UNDECORATED);
        }
        stage.setTitle(MESSAGES.getString("dialog.update.progress.title"));

        Pane rootPane = createRootPane();

        Scene scene = new Scene(rootPane, 250, 130);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Sets the download progress.
     * @param progress the progress between 0 and 1
     */
    void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    /**
     * @param callback a function to be called whenever the user clicks "cancel".
     */
    void onCancel(Runnable callback) {
        this.cancelCallback = callback;
    }

    void close() {
        this.stage.close();
    }

    private Pane createRootPane() {
        VBox pane = new VBox();
        pane.setPadding(new Insets(PADDING));

        Label updateMessage = new Label(MESSAGES.getString("dialog.update.progress.message"));
        Text dialogText = new Text(MESSAGES.getString("dialog.update.progress.autoRestart"));
        Button cancelButton = new Button(MESSAGES.getString("dialog.update.progress.cancel"));
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> cancelCallback.run());
        progressBar = new ProgressBar();
        progressBar.setProgress(0);

        pane.getChildren().addAll(updateMessage, dialogText, progressBar, cancelButton);
        VBox.setVgrow(progressBar, Priority.ALWAYS);
        cancelButton.setAlignment(Pos.BOTTOM_RIGHT);

        return pane;
    }
}
