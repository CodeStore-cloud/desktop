package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadSnippetCollectionController {
    private ListSnippets listSnippetsUseCase;
    private ReadLanguage readLanguageUseCase;
    private ReadTags readTagsUseCase;

    @Autowired
    public ReadSnippetCollectionController(
            @Nonnull ListSnippets listSnippetsUseCase,
            @Nonnull ReadLanguage readLanguageUseCase,
            ReadTags readTagsUseCase
    ) {
        this.listSnippetsUseCase = listSnippetsUseCase;
        this.readLanguageUseCase = readLanguageUseCase;
        this.readTagsUseCase = readTagsUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets(
            @RequestParam(value = "filter[language]", required = false) String languageId,
            @RequestParam(value = "filter[tags]", required = false) String tagCsvList
    ) throws LanguageNotExistsException, TagNotExistsException {
        var language = getLanguageById(languageId);
        var tags = getTagsFromCsv(tagCsvList);
        var filterProperties = new FilterProperties(language, tags);

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

    @Nullable
    private Collection<String> getTagsFromCsv(@Nullable String csvString) throws TagNotExistsException {
        if (csvString == null) {
            return null;
        }

        return readTagsUseCase.readTags(csvString.split(","));
    }
}
