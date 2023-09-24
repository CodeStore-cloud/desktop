package cloud.codestore.core.api.languages;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.listlanguages.ListLanguages;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LanguageController.class)
@Import({TestConfig.class, LanguageController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /languages/{languageId}")
class LanguageResourceTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ListLanguages listLanguagesUseCase;
    @MockBean
    private ReadLanguage readLanguageUseCase;

    @ParameterizedTest
    @MethodSource("languageStream")
    @DisplayName("returns a single programming languages")
    void returnLanguage(Language language) throws Exception {
        when(readLanguageUseCase.read(anyInt())).thenReturn(language);
        mockMvc.perform(get("/languages/" + language.getId()))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data.type", is("language")))
               .andExpect(jsonPath("$.data.id", is(String.valueOf(language.getId()))))
               .andExpect(jsonPath("$.data.attributes.name", is(language.getName())))
               .andExpect(jsonPath("$.data.relationships.snippets.links.related", is("http://localhost:8080/snippets?filter%5Blanguage%5D=" + language.getId())))
               .andExpect(jsonPath("$.data.links.self").exists());
    }

    private static Stream<Arguments> languageStream() {
        return Arrays.stream(Language.values())
                     .map(Arguments::of);
    }
}
