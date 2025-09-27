package cloud.codestore.core.usecases.listlanguages;

import cloud.codestore.core.Injectable;
import cloud.codestore.core.Language;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Use case: get all available programming languages.
 */
@Injectable
public class ListLanguages {
    @Nonnull
    public List<Language> list() {
        return Arrays.stream(Language.values())
                     .sorted(new LanguageComparator())
                     .toList();
    }
}
