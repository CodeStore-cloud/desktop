package cloud.codestore.client.usecases.readlanguages;

import cloud.codestore.client.Language;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use Case: read all programming languages.
 */
public interface ReadLanguagesUseCase {
    /**
     * @return an ordered list containing all available programming languages.
     */
    @Nonnull
    List<Language> readLanguages();
}
