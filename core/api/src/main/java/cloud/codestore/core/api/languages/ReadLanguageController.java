package cloud.codestore.core.api.languages;

import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = LanguageCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadLanguageController {
    private ReadLanguage readLanguageUseCase;

    @Autowired
    public ReadLanguageController(@Nonnull ReadLanguage readLanguageUseCase) {
        this.readLanguageUseCase = readLanguageUseCase;
    }

    @GetMapping("/{languageId}")
    public JsonApiDocument getSnippet(@PathVariable("languageId") int languageId) throws LanguageNotExistsException {
        var lang = readLanguageUseCase.read(languageId);
        return new LanguageResource(lang).asDocument();
    }
}
