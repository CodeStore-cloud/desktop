package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;

import javax.annotation.Nonnull;

public interface SnippetForm {
    void setEditable(boolean editable);

    void visit(@Nonnull Snippet snippet);

    void visit(@Nonnull SnippetBuilder builder);
}
