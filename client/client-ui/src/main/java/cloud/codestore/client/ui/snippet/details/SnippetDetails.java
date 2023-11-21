package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import javax.annotation.Nonnull;
import java.util.List;

@FxController
public class SnippetDetails {
    @FXML
    private FlowPane tagPane;

    public void setTags(@Nonnull List<String> tags) {
        var tagNodes = tags.stream().map(Label::new).toList();
        tagPane.getChildren().setAll(tagNodes);
    }
}
