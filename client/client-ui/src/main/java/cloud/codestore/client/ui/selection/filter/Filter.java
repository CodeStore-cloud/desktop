package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.UiMessages;
import cloud.codestore.client.ui.selection.ToggleFilterEvent;
import cloud.codestore.client.ui.selection.ToggleSortEvent;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

@FxController
public class Filter {
    private final ReadLanguagesUseCase readLanguagesUseCase;
    private final EventBus eventBus;

    public Filter(ReadLanguagesUseCase readLanguagesUseCase, EventBus eventBus) {
        this.readLanguagesUseCase = readLanguagesUseCase;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private Pane filterPanel;
    @FXML
    private TextField tagsInput;
    @FXML
    private ComboBox<LanguageItem> languageSelection;

    @FXML
    private void initialize() {
        filterPanel.managedProperty().bind(filterPanel.visibleProperty());
        filterPanel.setVisible(false);

        handleTagInput();
        fillLanguageSelection();
    }

    @Subscribe
    private void toggle(@Nonnull ToggleFilterEvent event) {
        filterPanel.setVisible(!filterPanel.isVisible());
    }

    @Subscribe
    private void toggle(@Nonnull ToggleSortEvent event) {
        filterPanel.setVisible(false);
    }

    private void handleTagInput() {
        tagsInput.textProperty().addListener((field, oldValue, newValue) -> triggerEvent());
    }

    private void fillLanguageSelection() {
        LanguageItem all = new LanguageItem(null, UiMessages.get("filter.language.all"));
        languageSelection.getItems().add(all);

        List<Language> languages = readLanguagesUseCase.readLanguages();
        for (Language language : languages) {
            LanguageItem item = new LanguageItem(language, language.name());
            languageSelection.getItems().add(item);
        }

        languageSelection.getSelectionModel().selectFirst(); // default selection - does not trigger filter event
    }

    @FXML
    void triggerEvent() {
        String tagsRaw = tagsInput.getText();
        Set<String> tags = tagsRaw.isEmpty() ? null : Set.of(tagsRaw.split(" "));
        Language language = languageSelection.getValue().language();
        FilterProperties filterProperties = new FilterProperties(tags, language);
        eventBus.post(new FilterEvent(filterProperties));
    }
}
