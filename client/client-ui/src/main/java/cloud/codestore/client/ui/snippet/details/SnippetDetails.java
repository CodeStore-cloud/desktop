package cloud.codestore.client.ui.snippet.details;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.filter.QuickFilterEvent;
import cloud.codestore.client.ui.snippet.SnippetForm;
import com.google.common.eventbus.EventBus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@FxController
public class SnippetDetails implements SnippetForm {
    private static final DateTimeFormatter DATE_TIME_FORMATTER;

    @FXML
    public Pane details;
    @FXML
    private TextField tagsInput;
    @FXML
    private Pane quickFilterTags;
    @FXML
    private GridPane timestampContainer;
    @FXML
    private Pane creationTimeContainer;
    @FXML
    private Pane modificationTimeContainer;
    @FXML
    private Label creationTime;
    @FXML
    private Label modificationTime;

    private final EventBus eventBus;

    SnippetDetails(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    static {
        String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                FormatStyle.MEDIUM,
                FormatStyle.SHORT,
                Chronology.ofLocale(Locale.getDefault()),
                Locale.getDefault()
        );
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(pattern);
    }

    @FXML
    private void initialize() {
        tagsInput.managedProperty().bind(tagsInput.visibleProperty());
        quickFilterTags.managedProperty().bind(quickFilterTags.visibleProperty());
    }

    @Override
    public void setEditing(boolean editing) {
        tagsInput.setVisible(editing);
        quickFilterTags.setVisible(!editing);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        String tagsString = String.join(" ", snippet.getTags());
        tagsInput.setText(tagsString);

        quickFilterTags.getChildren().clear();
        for (String tag :  snippet.getTags()) {
            Label tagLabel = new Label(tag);
            tagLabel.setOnMouseClicked(event -> eventBus.post(new QuickFilterEvent(tag)));
            quickFilterTags.getChildren().add(tagLabel);
        }

        setCreationTime(snippet.getCreated());
        setModificationTime(snippet.getModified());
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        String text = tagsInput.getText();
        List<String> tags = text.isEmpty() ? Collections.emptyList() : List.of(text.split(" "));
        builder.tags(tags);
    }

    private void setCreationTime(@Nullable OffsetDateTime time) {
        setTimestampVisible(GridPane.getRowIndex(creationTime), time != null);
        if (time != null) {
            creationTime.setText(DATE_TIME_FORMATTER.format(time));
        }
    }

    private void setModificationTime(@Nullable OffsetDateTime time) {
        setTimestampVisible(GridPane.getRowIndex(modificationTime), time != null);
        if (time != null) {
            modificationTime.setText(DATE_TIME_FORMATTER.format(time));
        }
    }

    private void setTimestampVisible(int rowIndex, boolean visible) {
        timestampContainer.getChildren()
                          .stream()
                          .filter(node -> GridPane.getRowIndex(node) == rowIndex)
                          .forEach(node -> node.setVisible(visible));
    }
}
