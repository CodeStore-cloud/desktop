package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class AbstractSnippetController {
    private ReadLanguage readLanguageUseCase;

    @Autowired
    public AbstractSnippetController(@Nonnull ReadLanguage readLanguageUseCase) {
        this.readLanguageUseCase = readLanguageUseCase;
    }

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
