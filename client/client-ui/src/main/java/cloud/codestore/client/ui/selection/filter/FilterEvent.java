package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.usecases.listsnippets.FilterProperties;

/**
 * An event which is fired when the filter has changed.
 * @param filterProperties a {@link FilterProperties} object containing the filter information.
 */
public record FilterEvent(FilterProperties filterProperties) {}
