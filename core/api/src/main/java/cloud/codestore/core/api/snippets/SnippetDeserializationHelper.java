package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class SnippetDeserializationHelper {
    private ReadLanguage readLanguageUseCase;

    @Autowired
    SnippetDeserializationHelper(@Nonnull ReadLanguage readLanguageUseCase) {
        this.readLanguageUseCase = readLanguageUseCase;
    }

    /**
     * Retrieves the related {@link Language} from the provided {@link Relationship} or {@code null} if the
     * relationship does not link to a programming language.
     *
     * @param relationship a relationship.
     * @return the related programming language or {@code null}.
     * @throws LanguageNotExistsException if the related programming language does not exist.
     */
    @Nullable
    Language getLanguage(Relationship relationship) throws LanguageNotExistsException {
        if (relationship instanceof ToOneRelationship<?> toOneRelationship) {
            ResourceIdentifierObject identifier = toOneRelationship.getData();
            if (LanguageResource.RESOURCE_TYPE.equals(identifier.getType())) {
                try {
                    int languageId = Integer.parseInt(identifier.getId());
                    return readLanguageUseCase.read(languageId);
                } catch (NumberFormatException exception) {
                    throw new LanguageNotExistsException();
                }
            }
        }

        return null;
    }
}
