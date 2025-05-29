package cloud.codestore.client.ui.snippet.code;

import cloud.codestore.client.Language;
import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.FxController;
import cloud.codestore.client.ui.selection.filter.QuickFilterEvent;
import cloud.codestore.client.ui.snippet.SnippetForm;
import cloud.codestore.client.usecases.readlanguages.ReadLanguagesUseCase;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@FxController
public class SnippetCode implements SnippetForm {
    private static final Logger LOGGER = LogManager.getLogger(SnippetCode.class);

    private final ReadLanguagesUseCase readLanguagesUseCase;
    private final EventBus eventBus;

    @FXML
    private ComboBox<Language> languageSelection;
    @FXML
    private Label languageQuickfilter;
    @FXML
    private WebView browser;
    private Editor editor = new LoadingEditor();

    SnippetCode(ReadLanguagesUseCase readLanguagesUseCase, EventBus eventBus) {
        this.readLanguagesUseCase = readLanguagesUseCase;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @FXML
    private void initialize() {
        languageSelection.managedProperty().bind(languageSelection.visibleProperty());
        languageQuickfilter.managedProperty().bind(languageQuickfilter.visibleProperty());
        languageSelection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            languageQuickfilter.setText(newValue == null ? "" : newValue.toString());
        });

        long startTime = System.currentTimeMillis();
        browser.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                LOGGER.info("Editor loaded in {}ms", System.currentTimeMillis() - startTime);
                editor.loadingFinished();
            } else if (newState == Worker.State.FAILED) {
                throw new RuntimeException("Loading editor failed!");
            }
        });

        browser.getEngine().loadContent(editorHtml());
    }

    @Subscribe
    private void applicationReady(@Nonnull ApplicationReadyEvent event) {
        languageSelection.getItems().addAll(readLanguagesUseCase.readLanguages());
    }

    @FXML
    private void quickfilterLanguage() {
        Language language = languageSelection.getSelectionModel().getSelectedItem();
        eventBus.post(new QuickFilterEvent(language));
    }

    @Override
    public void setEditing(boolean editable) {
        languageSelection.setVisible(editable);
        languageQuickfilter.setVisible(!editable);
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

    private String editorHtml() {
        try {
            String html;
            try (InputStream htmlStream = Objects.requireNonNull(getClass().getResourceAsStream("editor/editor.html"))) {
                html = IOUtils.toString(new InputStreamReader(htmlStream));
            }

            String js;
            try (InputStream jsStream = Objects.requireNonNull(getClass().getResourceAsStream("editor/editor.js"))) {
                js = IOUtils.toString(new InputStreamReader(jsStream));
            }

            return html.replace("{editor.js}", js);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private interface Editor {
        void setEditing(boolean editable);

        String getContent();

        void setContent(Snippet snippet);

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
        public String getContent() {
            return "";
        }

        @Override
        public void setContent(Snippet snippet) {
            this.snippet = snippet;
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
        public String getContent() {
            return (String) browser.getEngine().executeScript("editor.getContent();");
        }

        @Override
        public void setContent(Snippet snippet) {
            String languageId = snippet.getLanguage() == null ? "" : snippet.getLanguage().id();
            browser.getEngine().executeScript("editor.setLanguage(\"" + languageId + "\");");

            String content = snippet.getCode() == null ? "" : snippet.getCode();
            content = escapeBackslashes(content);
            content = escapeDoubleQuotes(content);
            content = escapeLineBreaks(content);
            content = removeCarriageReturns(content);

            browser.getEngine().executeScript("editor.setContent(\"" + content + "\");");
        }

        private String escapeBackslashes(String content) {
            return content.replace("\\", "\\\\");
        }

        private String escapeDoubleQuotes(String content) {
            return content.replace("\"", "\\\"");
        }

        private String escapeLineBreaks(String content) {
            return content.replace("\n", "\\n");
        }

        private String removeCarriageReturns(String content) {
            return content.replace("\r", "");
        }

        @Override
        public void loadingFinished() {}
    }
}
