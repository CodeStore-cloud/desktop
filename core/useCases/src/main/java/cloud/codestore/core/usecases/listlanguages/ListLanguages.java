package cloud.codestore.core.usecases.listlanguages;

import cloud.codestore.core.Language;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use case: get all available programming languages.
 */
public class ListLanguages {
    /**
     * @return a list of all available programming languages.
     */
    @Nonnull
    public List<Language> list() {
        return List.of(Language.values());
    }
}
