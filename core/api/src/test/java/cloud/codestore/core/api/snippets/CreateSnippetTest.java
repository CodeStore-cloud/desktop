package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.api.DefaultLocale;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.ErrorHandler;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.createsnippet.CreateSnippet;
import cloud.codestore.core.usecases.createsnippet.NewSnippetDto;
import cloud.codestore.core.usecases.deletesnippet.DeleteSnippet;
import cloud.codestore.core.usecases.listsnippets.ListSnippets;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.core.usecases.readsnippet.ReadSnippet;
import cloud.codestore.core.validation.InvalidSnippetException;
import cloud.codestore.core.validation.SnippetProperty;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import cloud.codestore.jsonapi.relationship.Relationship;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import cloud.codestore.jsonapi.resource.ResourceObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(DefaultLocale.class)
@WebMvcTest(SnippetController.class)
@Import({TestConfig.class, SnippetController.class, ErrorHandler.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("POST /snippets")
class CreateSnippetTest {
    private static final String SNIPPET_ID = UUID.randomUUID().toString();

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ListSnippets listSnippetsUseCase;
    @MockBean
    private ReadSnippet readSnippetUseCase;
    @MockBean
    private CreateSnippet createSnippetUseCase;
    @MockBean
    private DeleteSnippet deleteSnippetUseCase;
    @MockBean
    private ReadLanguage readLanguageUseCase;

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
        var validationMessages = Map.of(
                SnippetProperty.TITLE, "invalid title",
                SnippetProperty.DESCRIPTION, "invalid description",
                SnippetProperty.CODE, "invalid code"
        );

        var exception = Mockito.mock(InvalidSnippetException.class);
        when(exception.getValidationMessages()).thenReturn(validationMessages);
        when(createSnippetUseCase.create(any(NewSnippetDto.class))).thenThrow(exception);

        sendRequest().andExpect(status().isBadRequest())
                     .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                     .andExpect(jsonPath("$.errors").isArray())
                     .andExpect(jsonPath("$.errors.length()", is(3)))
                     .andExpect(jsonPath("$.errors[*].code", everyItem(is("INVALID_SNIPPET"))))
                     .andExpect(jsonPath("$.errors[*].title", everyItem(is("Invalid Code Snippet"))))
                     .andExpect(jsonPath("$.errors[0].detail", is("invalid title")))
                     .andExpect(jsonPath("$.errors[0].source.pointer", is("/data/attributes/title")))
                     .andExpect(jsonPath("$.errors[1].detail", is("invalid description")))
                     .andExpect(jsonPath("$.errors[1].source.pointer", is("/data/attributes/description")))
                     .andExpect(jsonPath("$.errors[2].detail", is("invalid code")))
                     .andExpect(jsonPath("$.errors[2].source.pointer", is("/data/attributes/code")));
    }

    @Test
    @DisplayName("fails if the referred programming language does not exist")
    void languageNotFound() throws Exception {
        when(readLanguageUseCase.read(anyInt())).thenThrow(LanguageNotExistsException.class);
        sendRequest().andExpect(status().isNotFound())
                     .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
                     .andExpect(jsonPath("$.errors").isArray())
                     .andExpect(jsonPath("$.errors.length()", is(1)))
                     .andExpect(jsonPath("$.errors[0].code", is("NOT_FOUND")))
                     .andExpect(jsonPath("$.errors[0].title", is("Not Found")))
                     .andExpect(jsonPath("$.errors[0].detail", is("The programming language does not exist.")));
    }

    private ResultActions sendRequest() throws Exception {
        return mockMvc.perform(post("/snippets").content(body()).contentType(JsonApiDocument.MEDIA_TYPE));
    }

    private String body() throws JsonProcessingException {
        var resource = new ClientSnippet(
                testSnippet.getLanguage(),
                testSnippet.getTitle(),
                testSnippet.getDescription(),
                testSnippet.getCode()
        );

        return objectMapper.writeValueAsString(resource.asDocument());
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
