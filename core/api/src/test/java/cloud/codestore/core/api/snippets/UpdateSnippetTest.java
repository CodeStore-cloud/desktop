package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.SnippetNotExistsException;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PATCH /snippets/{snippetId}")
class UpdateSnippetTest extends SnippetControllerTest {
    private Snippet testSnippet;

    @BeforeEach
    void setUp() throws LanguageNotExistsException, SnippetNotExistsException {
        testSnippet = new SnippetBuilder().id(SNIPPET_ID)
                                          .language(Language.PYTHON)
                                          .title("A simple code snippet")
                                          .description("A short description")
                                          .code("print('Hello, World!')")
                                          .created(OffsetDateTime.now().minusHours(2))
                                          .modified(OffsetDateTime.now())
                                          .build();

        when(readSnippetUseCase.read(SNIPPET_ID)).thenReturn(testSnippet);
        when(readLanguageUseCase.read(testSnippet.getLanguage().getId())).thenReturn(testSnippet.getLanguage());
    }

    @Test
    @DisplayName("updates the code snippet")
    void updateSnippet() throws Exception {
        sendRequest().andExpect(status().isOk());

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
    @DisplayName("returns the updated code snippet")
    void returnCreatedSnippet() throws Exception {
        sendRequest().andExpect(status().isOk())
                     .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                     .andExpect(jsonPath("$.data.type", is("snippet")))
                     .andExpect(jsonPath("$.data.id", is(testSnippet.getId())))
                     .andExpect(jsonPath("$.data.attributes.title", is(testSnippet.getTitle())))
                     .andExpect(jsonPath("$.data.attributes.description", is(testSnippet.getDescription())))
                     .andExpect(jsonPath("$.data.attributes.code", is(testSnippet.getCode())))
                     .andExpect(jsonPath("$.data.attributes.created", is(testSnippet.getCreated().toString())))
                     .andExpect(jsonPath("$.data.attributes.modified", is(testSnippet.getModified().toString())))
                     .andExpect(jsonPath("$.data.relationships.language.links.related", is("http://localhost:8080/languages/18")))
                     .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/snippets/" + testSnippet.getId())));
    }

    @Test
    @DisplayName("returns an error if the code snippet is invalid")
    void invalidSnippet() throws Exception {
        var exception = Mockito.mock(InvalidSnippetException.class);
        when(exception.getValidationMessages()).thenReturn(Map.of(SnippetProperty.TITLE, "dummy message"));
        doThrow(exception).when(updateSnippetUseCase).update(any(UpdatedSnippetDto.class));

        sendRequest().andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 404 if the referred code snippet does not exist")
    void snippetNotFound() throws Exception {
        doThrow(SnippetNotExistsException.class).when(updateSnippetUseCase).update(any(UpdatedSnippetDto.class));
        sendRequest().andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 if the referred programming language does not exist")
    void languageNotFound() throws Exception {
        when(readLanguageUseCase.read(anyInt())).thenThrow(LanguageNotExistsException.class);
        sendRequest().andExpect(status().isNotFound());
    }

    private ResultActions sendRequest() throws Exception {
        ClientSnippet resource = new ClientSnippet(
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

        ClientSnippet(Language language, String title, String description, String code) {
            super("snippet");

            var languageIdentifier = new ResourceIdentifierObject("language", String.valueOf(language.getId()));
            this.language = new ToOneRelationship<>().setData(languageIdentifier);
            this.title = title;
            this.description = description;
            this.code = code;
        }
    }
}
