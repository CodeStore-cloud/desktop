package cloud.codestore.client.ui.selection.sort;

import cloud.codestore.client.usecases.listsnippets.SortProperties;

import javax.annotation.Nonnull;

record SortItem(@Nonnull SortProperties.SnippetProperty property, @Nonnull String label) {
    @Override
    public String toString() {
        return label;
    }
}
