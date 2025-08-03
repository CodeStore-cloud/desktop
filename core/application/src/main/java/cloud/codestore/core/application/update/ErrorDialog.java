package cloud.codestore.core.application.update;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * An error dialog that is shown if updating the application fails.
 * It also provides the possibility to report the error.
 */
class ErrorDialog extends AbstractDialog {
    private final Exception exception;

    ErrorDialog(Exception exception) {
        this.exception = exception;
    }

    private void reportError() {
        // TODO send error report
    }

    @Override
    String getTitle() {
        return getMessage("dialog.error.title");
    }

    @Override
    Node[] getElements() {
        TextFlow generalErrorMessage = new TextFlow();
        Hyperlink link = new Hyperlink(getMessage("dialog.error.message.homepage"));
        link.setFocusTraversable(false);
        link.setOnAction(event -> getHostServices().showDocument("https://codestore.cloud"));
        generalErrorMessage.getChildren().addAll(
                new Text(getMessage("dialog.error.message.1")),
                link,
                new Text(getMessage("dialog.error.message.2"))
        );

        return new Node[]{generalErrorMessage};
    }

    @Override
    Button[] getButtons() {
        Button reportButton = new Button(getMessage("dialog.error.report"));
        reportButton.setAlignment(Pos.BOTTOM_RIGHT);
        reportButton.setOnAction(event -> {
            reportError();
            close();
        });

        return new Button[]{reportButton};
    }
}
