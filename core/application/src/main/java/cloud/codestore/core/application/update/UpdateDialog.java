package cloud.codestore.core.application.update;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The dialog that shows the progress of downloading the application.
 */
class UpdateDialog extends AbstractDialog {
    private Runnable cancelCallback = () -> {};
    private ProgressBar progressBar;

    @Override
    void show() {
        super.show();
        setOnClose(cancelCallback);
    }

    /**
     * Sets the download progress.
     *
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

    @Override
    String getTitle() {
        return getMessage("dialog.update.progress.title");
    }

    @Override
    Node[] getElements() {
        Label updateMessage = new Label(getMessage("dialog.update.progress.message"));
        Label dialogText = new Label(getMessage("dialog.update.progress.autoRestart"));
        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setMinHeight(progressBar.getPrefHeight());
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(progressBar, Priority.ALWAYS);

        return new Node[]{updateMessage, dialogText, progressBar};
    }

    @Override
    Button[] getButtons() {
        Button cancelButton = new Button(getMessage("dialog.update.progress.cancel"));
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> cancelCallback.run());
        cancelButton.setAlignment(Pos.BOTTOM_RIGHT);

        return new Button[]{cancelButton};
    }
}
