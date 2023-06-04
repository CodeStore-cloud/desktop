package cloud.codestore.core.api.languages;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.DummyWebServerInitializedEvent;
import cloud.codestore.core.api.TestConfig;
import cloud.codestore.core.usecases.listlanguages.ListLanguages;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LanguageController.class)
@Import({TestConfig.class, LanguageController.class})
@ExtendWith(DummyWebServerInitializedEvent.class)
@DisplayName("GET /languages")
class LanguageCollectionResourceTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ListLanguages listLanguagesUseCase;

    @Test
    @DisplayName("returns all available programming languages")
    void returnLanguageCollection() throws Exception {
        when(listLanguagesUseCase.list()).thenReturn(languageList());
        mockMvc.perform(get("/languages"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data").isArray())
               .andExpect(jsonPath("$.data.length()", is(3)))
               .andExpect(jsonPath("$.data[*].type", everyItem(is("language"))))
               .andExpect(jsonPath("$.data[*].attributes.name").exists())
               .andExpect(jsonPath("$.data[*].links.self").exists());
    }

    private List<Language> languageList() {
        return List.of(Language.JAVA, Language.HTML, Language.PYTHON);
    }
}