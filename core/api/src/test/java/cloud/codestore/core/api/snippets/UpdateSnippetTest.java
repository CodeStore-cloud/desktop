package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.usecases.updatesnippet.UpdateSnippet;
import cloud.codestore.core.usecases.updatesnippet.UpdatedSnippetDto;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetProperty;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UpdateSnippetController.class)
@Import(UpdateSnippetController.class)
@DisplayName("PATCH /snippets/{snippetId}")
class UpdateSnippetTest extends SnippetControllerTest {


    @MockitoBean
    private ReadSnippet readSnippetUseCase;
    @MockitoBean
    private UpdateSnippet updateSnippetUseCase;
    @MockitoBean
    private SnippetDeserializationHelper deserializationHelper;
    private Snippet testSnippet;

    @BeforeEach
    void setUp() throws LanguageNotExistsException, SnippetNotExistsException {
        when(deserializationHelper.getLanguage(any())).thenReturn(Language.JAVA);

        testSnippet = Snippet.builder()
                             .id(SNIPPET_ID)
                             .language(Language.PYTHON)
                             .title("A simple code snippet")
                             .description("A short description")
                             .code("print('Hello, World!')")
                             .created(OffsetDateTime.now().minusHours(2))
                             .modified(OffsetDateTime.now())
                             .build();

        when(readSnippetUseCase.read(SNIPPET_ID)).thenReturn(testSnippet);
        when(deserializationHelper.getLanguage(any())).thenReturn(testSnippet.getLanguage());
    }

    @Test
    @DisplayName("updates the code snippet")
    void updateSnippet() throws Exception {
        sendRequest(SNIPPET_ID).andExpect(status().isOk());

        ArgumentCaptor<UpdatedSnippetDto> argument = ArgumentCaptor.forClass(UpdatedSnippetDto.class);
        verify(updateSnippetUseCase).update(argument.capture());
        UpdatedSnippetDto dto = argument.getValue();
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(testSnippet.getId());
        assertThat(dto.language()).isEqualTo(testSnippet.getLanguage());
        assertThat(dto.title()).isEqualTo(testSnippet.getTitle());
        assertThat(dto.description()).isEqualTo(testSnippet.getDescription());
        assertThat(dto.code()).isEqualTo(testSnippet.getCode());
    }

    @Test
    @DisplayName("returns 400 if the snippet-id in the path does not match the snippet id in the JSON document")
    void validateSnippetId() throws Exception {
        sendRequest("another-snippet-id")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()", is(1)))
                .andExpect(jsonPath("$.errors[0].code", is("INVALID_SNIPPET")))
                .andExpect(jsonPath("$.errors[0].title", is("Invalid Code Snippet")))
                .andExpect(jsonPath("$.errors[0].detail", is("The ID of the snippet does not match the ID in the URI.")))
                .andExpect(jsonPath("$.errors[0].source.pointer", is("/data/id")));
    }

    @Test
    @DisplayName("can alternatively be called with POST and X-HTTP-Method-Override")
    void updateSnippetViaPOST() throws Exception {
        ClientSnippet resource = new ClientSnippet(
                SNIPPET_ID,
                testSnippet.getLanguage(),
                testSnippet.getTitle(),
                testSnippet.getDescription(),
                testSnippet.getCode()
        );

        String requestBody = objectMapper.writeValueAsString(resource.asDocument());
        PATCHviaPOST(SNIPPET_URL, requestBody).andExpect(status().isOk());

        verify(updateSnippetUseCase).update(any(UpdatedSnippetDto.class));
    }

    @Test
    @DisplayName("returns the updated code snippet")
    void returnCreatedSnippet() throws Exception {
        sendRequest(SNIPPET_ID)
                .andExpect(status().isOk())
                .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                .andExpect(jsonPath("$.data.type", is("snippet")))
                .andExpect(jsonPath("$.data.id", is(testSnippet.getId())))
                .andExpect(jsonPath("$.data.attributes.title", is(testSnippet.getTitle())))
                .andExpect(jsonPath("$.data.attributes.description", is(testSnippet.getDescription())))
                .andExpect(jsonPath("$.data.attributes.code", is(testSnippet.getCode())))
                .andExpect(jsonPath("$.data.attributes.created", is(testSnippet.getCreated().toString())))
                .andExpect(jsonPath("$.data.attributes.modified", is(testSnippet.getModified().toString())))
                .andExpect(jsonPath("$.data.relationships.language.links.related", is("http://localhost:8080/languages/18")))
                .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/snippets/" + testSnippet.getId())))
                .andExpect(jsonPath("$.data.meta.operations").isArray())
                .andExpect(jsonPath("$.data.meta.operations.length()", is(2)))
                .andExpect(jsonPath("$.data.meta.operations[*].operation", everyItem(oneOf("updateSnippet", "deleteSnippet"))));
    }

    @Test
    @DisplayName("returns an error if the code snippet is invalid")
    void invalidSnippet() throws Exception {
        var exception = Mockito.mock(InvalidSnippetException.class);
        when(exception.getValidationMessages()).thenReturn(Map.of(SnippetProperty.TITLE, "dummy message"));
        doThrow(exception).when(updateSnippetUseCase).update(any(UpdatedSnippetDto.class));

        sendRequest(SNIPPET_ID).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 404 if the referred code snippet does not exist")
    void snippetNotFound() throws Exception {
        doThrow(SnippetNotExistsException.class).when(updateSnippetUseCase).update(any(UpdatedSnippetDto.class));
        sendRequest(SNIPPET_ID).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 if the referred programming language does not exist")
    void languageNotFound() throws Exception {
        when(deserializationHelper.getLanguage(any())).thenThrow(LanguageNotExistsException.class);
        sendRequest(SNIPPET_ID).andExpect(status().isNotFound());
    }

    private ResultActions sendRequest(String snippetId) throws Exception {
        ClientSnippet resource = new ClientSnippet(
                snippetId,
                testSnippet.getLanguage(),
                testSnippet.getTitle(),
                testSnippet.getDescription(),
                testSnippet.getCode()
        );

        String requestBody = objectMapper.writeValueAsString(resource.asDocument());
        return PATCH("/snippets/" + SNIPPET_ID, requestBody);
    }

    private static class ClientSnippet extends ResourceObject {
        @JsonProperty
        final Relationship language;
        @JsonProperty
        final String title;
        @JsonProperty
        final String description;
        @JsonProperty
        final String code;

        ClientSnippet(String id, Language language, String title, String description, String code) {
            super("snippet", id);

            var languageIdentifier = new ResourceIdentifierObject("language", String.valueOf(language.getId()));
            this.language = new ToOneRelationship<>().setData(languageIdentifier);
            this.title = title;
            this.description = description;
            this.code = code;
        }
    }
}
