package cloud.codestore.client.ui.history;

import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.list.RequestSnippetSelectionEvent;
import cloud.codestore.client.ui.selection.list.SnippetSelectedEvent;
import cloud.codestore.client.ui.snippet.SnippetDeletedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.Stack;

@FxController
public class History {
    /** Used to avoid handling SnippetSelectedEvents triggered by this class itself. */
    private boolean handleSelection = true;
    private String currentSnippet;
    private Stack<String> previousSnippets = new Stack<>();
    private Stack<String> nextSnippets = new Stack<>();
    private EventBus eventBus;

    @FXML
    private Pane historyPane;
    @FXML
    private Button prevSnippetButton;
    @FXML
    private Button nextSnippetButton;

    History(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        updateButtonStates();
    }

    @Subscribe
    private void addSnippet(@Nonnull SnippetSelectedEvent event) {
        if (handleSelection) {
            if (currentSnippet != null) {
                previousSnippets.push(currentSnippet);
            }

            currentSnippet = event.snippetUri();
            nextSnippets.clear();
            updateButtonStates();
        }
        handleSelection = true;
    }

    @Subscribe
    private void removeSnippet(@Nonnull SnippetDeletedEvent event) {
        currentSnippet = null;

        if (hasPreviousSnippets()) {
            selectPreviousSnippet();
        } else if (hasNextSnippets()) {
            selectNextSnippet();
        }
    }

    @FXML
    void selectPreviousSnippet() {
        if (hasPreviousSnippets()) {
            if (currentSnippet != null) {
                nextSnippets.push(currentSnippet);
            }

            currentSnippet = previousSnippets.pop();
            updateButtonStates();
            handleSelection = false;
            eventBus.post(new SnippetSelectedEvent(currentSnippet));
        }
    }

    @FXML
    void selectNextSnippet() {
        if (hasNextSnippets()) {
            if (currentSnippet != null) {
                previousSnippets.push(currentSnippet);
            }

            currentSnippet = nextSnippets.pop();
            updateButtonStates();
            handleSelection = false;
            eventBus.post(new RequestSnippetSelectionEvent(currentSnippet));
        }
    }

    public void setVisible(boolean visible) {
        historyPane.setVisible(visible);
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
