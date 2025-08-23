package cloud.codestore.client.ui.selection.list;

import javax.annotation.Nonnull;

/**
 * An event which is fired when the user selects a code snippet.
 * This event does not directly display the code snippet but only requests it.
 * In some cases (e.g. if the user is editing another code snippet) the selection may be denied.
 * The selection must be confirmed by a {@link SnippetSelectedEvent}.
 *
 * @param snippetUri the URI of the selected code snippet. Must not be {@code null}.
 */
public record RequestSnippetSelectionEvent(@Nonnull String snippetUri) {}
