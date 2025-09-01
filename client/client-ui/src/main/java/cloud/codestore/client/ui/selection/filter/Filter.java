package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.UiMessages;
import cloud.codestore.client.usecases.listsnippets.FilterProperties;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;
import java.util.*;

@FxController
public class Filter {
    private final ReadLanguagesUseCase readLanguagesUseCase;

    @FXML
    private Pane filterPanel;
    @FXML
    private TextField tagsInput;
    @FXML
    private ComboBox<LanguageItem> languageSelection;
    private boolean updatePropertiesEnabled = true;
    private ObjectProperty<FilterProperties> filterProperties = new SimpleObjectProperty<>(new FilterProperties());

    public Filter(ReadLanguagesUseCase readLanguagesUseCase, EventBus eventBus) {
        this.readLanguagesUseCase = readLanguagesUseCase;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        filterPanel.managedProperty().bind(filterPanel.visibleProperty());
        filterPanel.setVisible(false);
        tagsInput.textProperty().addListener((field, oldValue, newValue) -> filterChanged());
    }

    public void hide() {
        filterPanel.setVisible(false);
    }

    public void toggle() {
        filterPanel.setVisible(!filterPanel.isVisible());
        if (filterPanel.isVisible()) {
            tagsInput.requestFocus();
        }
    }

    public ObjectProperty<FilterProperties> filterProperties() {
        return filterProperties;
    }

    @Subscribe
    private void fillLanguageSelection(@Nonnull ApplicationReadyEvent event) {
        LanguageItem all = new LanguageItem(null, UiMessages.get("filter.language.all"));
        languageSelection.getItems().add(all);

        List<Language> languages = readLanguagesUseCase.readLanguages();
        for (Language language : languages) {
            LanguageItem item = new LanguageItem(language, language.name());
            languageSelection.getItems().add(item);
        }

        languageSelection.getSelectionModel().selectFirst(); // default selection - does not call filterChanged()
    }

    @Subscribe
    private void quickfilter(@Nonnull QuickFilterEvent event) {
        String tag = event.tag();
        if (tag != null) {
            List<String> tags = new ArrayList<>(List.of(tagsInput.getText().split(" ")));
            if (!tags.contains(tag)) {
                tags.add(tag);
                tagsInput.setText(String.join(" ",  tags));
            }
        }

        Language language = event.language();
        if (language != null) {
            languageSelection.getItems()
                             .stream()
                             .filter(item -> Objects.equals(item.language(), language))
                             .findFirst()
                             .ifPresent(item -> languageSelection.getSelectionModel().select(item));
        }
    }

    @FXML
    void filterChanged() {
        if (updatePropertiesEnabled) {
            String tagsInput = this.tagsInput.getText();
            Set<String> tags = tagsInput.isEmpty() ? null : new HashSet<>(List.of(tagsInput.split(" ")));
            Language language = languageSelection.getValue().language();
            filterProperties.set(new FilterProperties(tags, language));
        }
    }

    @FXML
    private void clearFilter() {
        updatePropertiesEnabled = false;
        this.tagsInput.clear();
        this.languageSelection.getSelectionModel().selectFirst();
        updatePropertiesEnabled = true;

        filterProperties.set(new FilterProperties());
    }
}
