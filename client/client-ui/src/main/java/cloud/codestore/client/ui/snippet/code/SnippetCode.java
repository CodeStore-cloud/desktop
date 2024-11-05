package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.snippet.SnippetForm;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

@FxController
public class SnippetCode implements SnippetForm {
    private static final Logger LOGGER = LogManager.getLogger(SnippetCode.class);

    private final ReadLanguagesUseCase readLanguagesUseCase;
    private final Path binDirectory;

    @FXML
    private ComboBox<Language> languageSelection;
    @FXML
    private WebView browser;
    private Editor editor = new LoadingEditor();

    SnippetCode(ReadLanguagesUseCase readLanguagesUseCase, Path binDirectory) {
        this.readLanguagesUseCase = readLanguagesUseCase;
        this.binDirectory = binDirectory;
    }

    @FXML
    private void initialize() {
        languageSelection.getItems().addAll(readLanguagesUseCase.readLanguages());

        long startTime = System.currentTimeMillis();
        browser.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                LOGGER.info("Editor loaded in {}ms", System.currentTimeMillis() - startTime);
                editor.loadingFinished();
            } else if (newState == Worker.State.FAILED) {
                throw new RuntimeException("Loading editor failed!");
            }
        });

        String editorUrl = binDirectory.resolve("editor.html").toUri().toString();
        LOGGER.info("Loading editor from location " + editorUrl + " ...");
        browser.getEngine().load(editorUrl);
    }

    @Override
    public void setEditing(boolean editable) {
        languageSelection.setDisable(!editable);
        editor.setEditing(editable);
    }

    @Override
    public void visit(@Nonnull Snippet snippet) {
        languageSelection.getSelectionModel().select(snippet.getLanguage());
        editor.setContent(snippet);
    }

    @Override
    public void visit(@Nonnull SnippetBuilder builder) {
        builder.language(languageSelection.getSelectionModel().getSelectedItem());
        builder.code(editor.getContent());
    }

    private interface Editor {
        void setEditing(boolean editable);

        void setContent(Snippet snippet);

        String getContent();

        void loadingFinished();
    }

    /**
     * The editor is loaded asynchronously.
     * This class represents the editor in its loading-state when it´s not ready yet.
     * Interactions are recorded and applied to the web-editor as soon as it´s ready.
     */
    private class LoadingEditor implements Editor {
        private boolean editable;
        private Snippet snippet;

        @Override
        public void setEditing(boolean editable) {
            this.editable = editable;
        }

        @Override
        public void setContent(Snippet snippet) {
            this.snippet = snippet;
        }

        @Override
        public String getContent() {
            return "";
        }

        @Override
        public void loadingFinished() {
            editor = new WebEditor(editable, snippet);
        }
    }

    /**
     * The editor is loaded asynchronously.
     * This class represents the fully loaded web-editor.
     */
    private class WebEditor implements Editor {
        WebEditor(boolean editable, @Nullable Snippet snippet) {
            setEditing(editable);
            if (snippet != null) {
                setContent(snippet);
            }
        }

        @Override
        public void setEditing(boolean editable) {
            browser.getEngine().executeScript("editor.setEditable(" + editable + ");");
        }

        @Override
        public void setContent(Snippet snippet) {
            String languageId = snippet.getLanguage() == null ? "" : snippet.getLanguage().id();
            browser.getEngine().executeScript("editor.setLanguage(\"" + languageId + "\");");

            String content = snippet.getCode() == null ? "" : snippet.getCode();
            content = content.replace("\\", "\\\\"); // \ -> \\
            content = content.replace("\"", "\\\""); // " -> \"
            content = content.replace("\n", "\\n");  // <newline> -> \n
            content = content.replace("\r", "");     // remove \r

            browser.getEngine().executeScript("editor.setContent(\"" + content + "\");");
        }

        @Override
        public String getContent() {
            return (String) browser.getEngine().executeScript("editor.getContent();");
        }

        @Override
        public void loadingFinished() {}
    }
}
