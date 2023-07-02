package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.core.usecases.createsnippet.CreateSnippet;
import cloud.codestore.core.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippet;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.document.SingleResourceDocument;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

@RestController
@RequestMapping(path = SnippetCollectionResource.PATH, produces = JsonApiDocument.MEDIA_TYPE)
public class SnippetController {
    private ListSnippets listSnippetsUseCase;
    private ReadSnippet readSnippetUseCase;
    private CreateSnippet createSnippetUseCase;
    private DeleteSnippet deleteSnippetUseCase;
    private ReadLanguage readLanguageUseCase;

    @Autowired
    public SnippetController(
            @Nonnull ListSnippets listSnippetsUseCase,
            @Nonnull ReadSnippet readSnippetUseCase,
            @Nonnull CreateSnippet createSnippetUseCase,
            @Nonnull DeleteSnippet deleteSnippetUseCase,
            @Nonnull ReadLanguage readLanguageUseCase
    ) {
        this.listSnippetsUseCase = listSnippetsUseCase;
        this.readSnippetUseCase = readSnippetUseCase;
        this.createSnippetUseCase = createSnippetUseCase;
        this.deleteSnippetUseCase = deleteSnippetUseCase;
        this.readLanguageUseCase = readLanguageUseCase;
    }

    @GetMapping
    public JsonApiDocument getSnippets() {
        var snippets = listSnippetsUseCase.list();
        return new SnippetCollectionResource(snippets);
    }

    @GetMapping("/{snippetId}")
    public JsonApiDocument getSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippet = readSnippetUseCase.read(snippetId);
        return new SnippetResource(snippet).asDocument();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonApiDocument createSnippet(
            @RequestBody SingleResourceDocument<SnippetResource> document,
            HttpServletResponse response
    ) throws InvalidSnippetException, LanguageNotExistsException {
        SnippetResource resource = document.getData();
        NewSnippetDto dto = new NewSnippetDto(
                getLanguage(resource.getLanguage()),
                resource.getTitle(),
                resource.getCode(),
                resource.getDescription()
        );

        Snippet createdSnippet = createSnippetUseCase.create(dto);

        SnippetResource snippetResource = new SnippetResource(createdSnippet);
        response.setHeader(HttpHeaders.LOCATION, snippetResource.getSelfLink());
        return snippetResource.asDocument();
    }

    @DeleteMapping("/{snippetId}")
    public JsonApiDocument deleteSnippet(@PathVariable("snippetId") String snippetId) throws SnippetNotExistsException {
        var snippetResource = getSnippet(snippetId);
        deleteSnippetUseCase.delete(snippetId);
        return snippetResource;
    }

    private Language getLanguage(Relationship relationship) throws LanguageNotExistsException {
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
