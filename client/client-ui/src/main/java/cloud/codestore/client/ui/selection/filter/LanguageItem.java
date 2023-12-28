package cloud.codestore.client.ui.selection.filter;

import cloud.codestore.client.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

record LanguageItem(@Nullable Language language, @Nonnull String label) {
    @Override
    public String toString() {
        return label;
    }
}
