package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

@FxController
public class SnippetCode {
    private ReadLanguagesUseCase readLanguagesUseCase;

    @FXML
    private ComboBox<Language> languageSelection;
    @FXML
    private TextArea snippetCode;

    public SnippetCode(ReadLanguagesUseCase readLanguagesUseCase) {
        this.readLanguagesUseCase = readLanguagesUseCase;
    }

    @FXML
    private void initialize() {
        languageSelection.getItems().addAll(readLanguagesUseCase.readLanguages());
        languageSelection.setEditable(false);
        snippetCode.setEditable(false);
    }

    public void setLanguage(Language language) {
        languageSelection.getSelectionModel().select(language);
    }

    public Language getLanguage() {
        return languageSelection.getSelectionModel().getSelectedItem();
    }

    public String getText() {
        return snippetCode.getText();
    }

    public void setText(String description) {
        snippetCode.setText(description);
    }

    public void bindEditing(BooleanProperty editingProperty) {
        languageSelection.editableProperty().bind(editingProperty);
        snippetCode.editableProperty().bind(editingProperty);
    }
}
