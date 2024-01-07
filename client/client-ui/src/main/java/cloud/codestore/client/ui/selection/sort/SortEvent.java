package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.usecases.listsnippets.SortProperties;

/**
 * An event which is fired when the sort has changed.
 *
 * @param sortProperties a {@link SortProperties} object containing the sort information.
 */
public record SortEvent(SortProperties sortProperties) {}
