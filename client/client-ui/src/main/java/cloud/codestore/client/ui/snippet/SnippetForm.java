package cloud.codestore.client.ui.snippet;

import cloud.codestore.client.Snippet;
import cloud.codestore.client.SnippetBuilder;

import javax.annotation.Nonnull;

/**
 * Represents a form holding a part of a code snippet.
 */
public interface SnippetForm {
    /**
     * Enables or disables editing of this form.
     *
     * @param editing whether this form should be editable.
     */
    void setEditing(boolean editing);

    /**
     * Shows the content of the given code snippet in this form.
     *
     * @param snippet a code snippet.
     */
    default void visit(@Nonnull Snippet snippet) {}

    /**
     * Fills the provided {@link SnippetBuilder} with the content of this form.
     *
     * @param builder a snippet builder.
     */
    default void visit(@Nonnull SnippetBuilder builder) {}
}
