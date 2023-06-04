package cloud.codestore.core.usecases.readlanguage;

import cloud.codestore.core.Language;

import javax.annotation.Nonnull;

/**
 * Use case: read a programming language by its id.
 */
public class ReadLanguage {

    /**
     * Reads the programming language with the given id.
     *
     * @param id the id of a programming language.
     * @return the corresponding programming language.
     * @throws LanguageNotExistsException if the programming language does not exist.
     */
    @Nonnull
    public Language read(int id) throws LanguageNotExistsException {
        for (Language language : Language.values()) {
            if (language.getId() == id) {
                return language;
            }
        }

        throw new LanguageNotExistsException();
    }
}
