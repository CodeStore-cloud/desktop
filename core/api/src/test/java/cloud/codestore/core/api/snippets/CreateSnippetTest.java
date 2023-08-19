package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.usecases.createsnippet.CreateSnippet;
import cloud.codestore.core.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreateSnippetController.class)
@Import(CreateSnippetController.class)
@DisplayName("POST /snippets")
class CreateSnippetTest extends SnippetControllerTest {

    @MockBean
    protected CreateSnippet createSnippetUseCase;
    private Snippet testSnippet;

    @BeforeEach
    void setUp() throws InvalidSnippetException, LanguageNotExistsException {
        testSnippet = new SnippetBuilder().id(SNIPPET_ID)
                                          .language(Language.PYTHON)
                                          .title("A simple code snippet")
                                          .description("A short description")
                                          .code("print('Hello, World!')")
                                          .build();

        when(createSnippetUseCase.create(any(NewSnippetDto.class))).thenReturn(testSnippet);
        when(readLanguageUseCase.read(testSnippet.getLanguage().getId())).thenReturn(testSnippet.getLanguage());
    }

    @Test
    @DisplayName("creates a code snippet")
    void createSnippet() throws Exception {
        sendRequest().andExpect(status().isCreated());

        ArgumentCaptor<NewSnippetDto> argument = ArgumentCaptor.forClass(NewSnippetDto.class);
        verify(createSnippetUseCase).create(argument.capture());
        NewSnippetDto dto = argument.getValue();
        assertThat(dto).isNotNull();
        assertThat(dto.language()).isEqualTo(testSnippet.getLanguage());
        assertThat(dto.title()).isEqualTo(testSnippet.getTitle());
        assertThat(dto.description()).isEqualTo(testSnippet.getDescription());
        assertThat(dto.code()).isEqualTo(testSnippet.getCode());
    }

    @Test
    @DisplayName("returns the created code snippet")
    void returnCreatedSnippet() throws Exception {
        sendRequest().andExpect(status().isCreated())
                     .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                     .andExpect(jsonPath("$.data.type", is("snippet")))
                     .andExpect(jsonPath("$.data.id", is(testSnippet.getId())))
                     .andExpect(jsonPath("$.data.attributes.title", is(testSnippet.getTitle())))
                     .andExpect(jsonPath("$.data.attributes.description", is(testSnippet.getDescription())))
                     .andExpect(jsonPath("$.data.attributes.code", is(testSnippet.getCode())))
                     .andExpect(jsonPath("$.data.attributes.created", is(testSnippet.getCreated().toString())))
                     .andExpect(jsonPath("$.data.relationships.language.links.related", is("http://localhost:8080/languages/18")))
                     .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/snippets/" + testSnippet.getId())));
    }

    @Test
    @DisplayName("includes the link of the new snippet in the Location header")
    void includeLocationHeader() throws Exception {
        String snippetLink = "http://localhost:8080/snippets/" + testSnippet.getId();
        sendRequest().andExpect(status().isCreated())
                     .andExpect(header().string("Location", snippetLink));
    }

    @Test
    @DisplayName("returns an error if the code snippet is invalid")
    void invalidSnippet() throws Exception {
        var exception = Mockito.mock(InvalidSnippetException.class);
        when(exception.getValidationMessages()).thenReturn(Map.of(SnippetProperty.TITLE, "dummy message"));
        when(createSnippetUseCase.create(any(NewSnippetDto.class))).thenThrow(exception);

        sendRequest().andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("fails if the referred programming language does not exist")
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
        return POST("/snippets", requestBody);
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
