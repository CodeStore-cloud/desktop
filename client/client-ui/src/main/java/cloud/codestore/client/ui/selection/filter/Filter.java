package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.UiMessages;
import cloud.codestore.client.ui.selection.sort.ToggleSortEvent;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private boolean filterEventEnabled = true;

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

    @Subscribe
    private void quickfilter(@Nonnull QuickFilterEvent event) {
        Language language = event.language();
        languageSelection.getItems()
                         .stream()
                         .filter(item -> Objects.equals(item.language(), language))
                         .findFirst()
                         .ifPresent(item -> languageSelection.getSelectionModel().select(item));
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
        if (filterEventEnabled) {
            String tagsInput = this.tagsInput.getText();
            Set<String> tags = tagsInput.isEmpty() ? null : new HashSet<>(List.of(tagsInput.split(" ")));
            Language language = languageSelection.getValue().language();
            FilterProperties filterProperties = new FilterProperties(tags, language);
            eventBus.post(new FilterEvent(filterProperties));
        }
    }

    @FXML
    void clearFilter() {
        doWithoutFiringFilterEvent(() -> {
            this.tagsInput.clear();
            this.languageSelection.getSelectionModel().selectFirst();
        });
        eventBus.post(new FilterEvent(new FilterProperties()));
    }

    private void doWithoutFiringFilterEvent(Runnable runnable) {
        try {
            filterEventEnabled = false;
            runnable.run();
        } finally {
            filterEventEnabled = true;
        }
    }
}
