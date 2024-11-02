package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

import javax.annotation.Nonnull;

@FxController
public class SnippetCode implements SnippetForm {
    private ReadLanguagesUseCase readLanguagesUseCase;

    @FXML
    private ComboBox<Language> languageSelection;
    @FXML
    private TextArea snippetCode;

    SnippetCode(ReadLanguagesUseCase readLanguagesUseCase) {
        this.readLanguagesUseCase = readLanguagesUseCase;
    }

    @FXML
    private void initialize() {
        languageSelection.getItems().addAll(readLanguagesUseCase.readLanguages());
    }

    @Override
    public void setEditing(boolean editable) {
        languageSelection.setDisable(!editable);
        snippetCode.setEditable(editable);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        languageSelection.getSelectionModel().select(snippet.getLanguage());
        snippetCode.setText(snippet.getCode());
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        builder.language(languageSelection.getSelectionModel().getSelectedItem());
        builder.code(snippetCode.getText());
    }
}
