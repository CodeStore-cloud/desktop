package cloud.codestore.core.api.languages;

import cloud.codestore.core.usecases.listlanguages.ListLanguages;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = LanguageCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadLanguageCollectionController {
    private ListLanguages listLanguagesUseCase;

    @Autowired
    public ReadLanguageCollectionController(@Nonnull ListLanguages listLanguagesUseCase) {
        this.listLanguagesUseCase = listLanguagesUseCase;
    }

    @GetMapping
    public JsonApiDocument getLanguages() {
        var languages = listLanguagesUseCase.list();
        return new LanguageCollectionResource(languages);
    }
}
