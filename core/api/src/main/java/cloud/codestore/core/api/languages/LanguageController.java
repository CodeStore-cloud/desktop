package cloud.codestore.core.api.languages;

import cloud.codestore.core.usecases.listlanguages.ListLanguages;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = LanguageCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class LanguageController {
    private ListLanguages listLanguagesUseCase;

    @Autowired
    public LanguageController(ListLanguages listLanguagesUseCase) {
        this.listLanguagesUseCase = listLanguagesUseCase;
    }

    @GetMapping
    public JsonApiDocument getLanguages() {
        var languages = listLanguagesUseCase.list();
        return new LanguageCollectionResource(languages);
    }
}
