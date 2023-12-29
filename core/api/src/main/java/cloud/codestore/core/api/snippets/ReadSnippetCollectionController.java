package cloud.codestore.core.api.snippets;

import cloud.codestore.core.api.InvalidParameterException;
import cloud.codestore.core.usecases.listsnippets.FilterProperties;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.listsnippets.PageNotExistsException;
import cloud.codestore.core.usecases.listsnippets.SortProperties;
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
import java.util.*;

import static cloud.codestore.core.api.snippets.SnippetCollectionResource.PATH;
import static cloud.codestore.core.api.snippets.SnippetCollectionResource.getLink;
import static cloud.codestore.core.usecases.listsnippets.SortProperties.SnippetProperty;

@RestController
@RequestMapping(path = PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class ReadSnippetCollectionController {
    private ListSnippets listSnippetsUseCase;

    @Autowired
    public ReadSnippetCollectionController(@Nonnull ListSnippets listSnippetsUseCase) {
        this.listSnippetsUseCase = listSnippetsUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets(
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String search,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page[number]", required = false, defaultValue = "1") String pageParam,
            @RequestParam(value = "filter[language]", required = false, defaultValue = "") String languageName,
            @RequestParam(value = "filter[tags]", required = false, defaultValue = "") String tagCsvList
    ) throws InvalidParameterException, PageNotExistsException {
        var tags = getTagsFromCsv(tagCsvList);
        var filterProperties = new FilterProperties(languageName, tags);
        var sortProperties = parseSortParameter(sort);
        var pageNumber = parsePageNumber(pageParam);

        var page = listSnippetsUseCase.list(search, filterProperties, sortProperties, pageNumber);
        var document = new SnippetCollectionResource(page.snippets());

        var urlParameters = new HashMap<String, Object>(5);
        urlParameters.put("searchQuery", search.isEmpty() ? null : search);
        urlParameters.put("sort", sort);
        urlParameters.put("filter[language]", languageName);
        urlParameters.put("filter[tags]", tagCsvList);
        addPaginationLinks(document, urlParameters, pageNumber, page.totalPages());

        return document;
    }

    @Nonnull
    private Collection<String> getTagsFromCsv(@Nonnull String csvString) {
        return csvString.isEmpty() ? Collections.emptySet() : Set.of(csvString.split(","));
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

    private void addPaginationLinks(
            JsonApiDocument document,
            Map<String, Object> urlParameters,
            int pageNumber,
            int totalPages
    ) {
        if (totalPages > 1) {
            urlParameters.put("page[number]", 1);
            document.addLink(new Link(Link.FIRST, getLink(urlParameters)));
            urlParameters.put("page[number]", totalPages);
            document.addLink(new Link(Link.LAST, getLink(urlParameters)));
        }
        if (pageNumber < totalPages) {
            urlParameters.put("page[number]", pageNumber + 1);
            document.addLink(new Link(Link.NEXT, getLink(urlParameters)));
        }
        if (pageNumber > 1) {
            urlParameters.put("page[number]", pageNumber - 1);
            document.addLink(new Link(Link.PREV, getLink(urlParameters)));
        }
    }
}
