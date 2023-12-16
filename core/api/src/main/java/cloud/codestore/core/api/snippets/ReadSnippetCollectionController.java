package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.TagNotExistsException;
import cloud.codestore.core.api.InvalidParameterException;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.listsnippets.PageNotExistsException;
import cloud.codestore.core.usecases.listsnippets.SortProperties;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.core.usecases.readtags.ReadTags;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.link.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static cloud.codestore.core.api.snippets.SnippetCollectionResource.PATH;
import static cloud.codestore.core.api.snippets.SnippetCollectionResource.getLink;
import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty;

@RestController
@RequestMapping(path = PATH, produces = JsonApiDocument.MEDIA_TYPE)
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
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String search,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page[number]", required = false, defaultValue = "1") String pageParam,
            @RequestParam(value = "filter[language]", required = false) String languageId,
            @RequestParam(value = "filter[tags]", required = false) String tagCsvList
    ) throws LanguageNotExistsException, TagNotExistsException, InvalidParameterException, PageNotExistsException {
        var language = getLanguageById(languageId);
        var tags = getTagsFromCsv(tagCsvList);
        var filterProperties = new FilterProperties(language, tags);
        var sortProperties = parseSortParameter(sort);
        var pageNumber = parsePageNumber(pageParam);

        var page = listSnippetsUseCase.list(search, filterProperties, sortProperties, pageNumber);

        var document = new SnippetCollectionResource(page.snippets())
                .addLink(new Link(Link.FIRST, getLink(1)))
                .addLink(new Link(Link.LAST, getLink(page.totalPages())));

        if (pageNumber < page.totalPages())
            document.addLink(new Link(Link.NEXT, getLink(pageNumber + 1)));
        if (pageNumber > 1)
            document.addLink(new Link(Link.PREV, getLink(pageNumber - 1)));

        return document;
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

    @Nullable
    private SortProperties parseSortParameter(@Nullable String sortParameter) throws InvalidParameterException {
        if (StringUtils.hasText(sortParameter)) {
            try {
                sortParameter = sortParameter.toUpperCase();
                boolean asc = !sortParameter.startsWith("-");
                var snippetProperty = SnippetProperty.valueOf(asc ? sortParameter : sortParameter.substring(1));
                return new SortProperties(snippetProperty, asc);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("sort");
            }
        }

        return null;
    }

    private int parsePageNumber(@Nonnull String pageNumber) throws InvalidParameterException {
        try {
            return Integer.parseInt(pageNumber);
        } catch (NumberFormatException exception) {
            throw new InvalidParameterException("page[number]");
        }
    }
}
