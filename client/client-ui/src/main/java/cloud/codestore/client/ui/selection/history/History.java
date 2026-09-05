package cloud.codestore.client.ui.selection.history;

import cloud.codestore.client.Injectable;
import cloud.codestore.client.ui.snippet.SnippetForm;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.Stack;

/**
 * The history component to navigate between the recently selected code snippets.
 * The history is logically part of the selection but the UI component is located in the snippet editor.
 */
@Injectable
public class History implements SnippetForm {
    /**
     * Used to avoid handling snippet selection caused by this class itself.
     */
    private boolean handleSelection = true;
    private Stack<String> previousSnippets = new Stack<>();
    private Stack<String> nextSnippets = new Stack<>();
    private StringProperty selectedSnippetProperty;

    @FXML
    private Pane historyPane;
    @FXML
    private Button prevSnippetButton;
    @FXML
    private Button nextSnippetButton;

    @FXML
    private void initialize() {
        updateButtonStates();
    }

    public void setSelectedSnippetProperty(@Nonnull StringProperty selectedSnippetProperty) {
        this.selectedSnippetProperty = selectedSnippetProperty;
        selectedSnippetProperty.addListener((observable, previousSnippetUri, currentSnippetUri) -> {
            if (handleSelection) {
                if (previousSnippetUri != null && !previousSnippetUri.isEmpty()) {
                    previousSnippets.push(previousSnippetUri);
                }

                nextSnippets.clear();
                updateButtonStates();
            }
            handleSelection = true;
        });
    }

    /**
     * Removes the current snippet from the history.
     * If a previous or next snippet exists, it will be selected.
     */
    public void removeCurrentSnippet() {
        if (hasPreviousSnippets()) {
            selectPreviousSnippetWithoutAddingCurrentSnippetToHistory();
        } else if (hasNextSnippets()) {
            selectNextSnippetWithoutAddingCurrentSnippetToHistory();
        } else {
            handleSelection = false;
            selectedSnippetProperty.set("");
        }
    }

    @FXML
    private void selectPreviousSnippet() {
        if (hasPreviousSnippets()) {
            String currentSnippet = selectedSnippetProperty.get();
            if (!currentSnippet.isEmpty()) {
                nextSnippets.push(currentSnippet);
            }

            selectPreviousSnippetWithoutAddingCurrentSnippetToHistory();
        }
    }

    private void selectPreviousSnippetWithoutAddingCurrentSnippetToHistory() {
        String previousSnippet = previousSnippets.pop();
        updateButtonStates();
        handleSelection = false;
        selectedSnippetProperty.set(previousSnippet);
    }

    @FXML
    private void selectNextSnippet() {
        if (hasNextSnippets()) {
            String currentSnippet = selectedSnippetProperty.get();
            if (!currentSnippet.isEmpty()) {
                previousSnippets.push(currentSnippet);
            }

            selectNextSnippetWithoutAddingCurrentSnippetToHistory();
        }
    }

    private void selectNextSnippetWithoutAddingCurrentSnippetToHistory() {
        String nextSnippet = nextSnippets.pop();
        updateButtonStates();
        handleSelection = false;
        selectedSnippetProperty.set(nextSnippet);
    }

    @Override
    public void setEditing(boolean editing) {
        historyPane.setVisible(!editing);
    }

    private boolean hasPreviousSnippets() {
        return !previousSnippets.isEmpty();
    }

    private boolean hasNextSnippets() {
        return !nextSnippets.isEmpty();
    }

    private void updateButtonStates() {
        prevSnippetButton.setDisable(previousSnippets.isEmpty());
        nextSnippetButton.setDisable(nextSnippets.isEmpty());
    }
}
