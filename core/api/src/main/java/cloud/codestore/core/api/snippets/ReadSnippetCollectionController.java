package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadSnippetCollectionController {
    private ListSnippets listSnippetsUseCase;
    private ReadLanguage readLanguageUseCase;

    @Autowired
    public ReadSnippetCollectionController(
            @Nonnull ListSnippets listSnippetsUseCase,
            @Nonnull ReadLanguage readLanguageUseCase
    ) {
        this.listSnippetsUseCase = listSnippetsUseCase;
        this.readLanguageUseCase = readLanguageUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets(
            @RequestParam(value = "filter[language]", required = false) String languageId
    ) throws LanguageNotExistsException {
        FilterProperties filterProperties = new FilterProperties(getLanguageById(languageId));

        var snippets = listSnippetsUseCase.list(filterProperties);
        return new SnippetCollectionResource(snippets);
    }

    @Nullable
    private Language getLanguageById(@Nullable String languageId) throws LanguageNotExistsException {
        if (languageId == null) {
            return null;
        }

        try {
            return readLanguageUseCase.read(Integer.parseInt(languageId));
        } catch (NumberFormatException exception) {
            throw new LanguageNotExistsException();
        }
    }
}
