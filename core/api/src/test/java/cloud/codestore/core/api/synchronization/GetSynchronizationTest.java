package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.AbstractControllerTest;
import cloud.codestore.core.usecases.synchronizesnippets.*;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SynchronizationController.class)
@Import(SynchronizationController.class)
@DisplayName("GET /synchronizations/{id}")
class GetSynchronizationTest extends AbstractControllerTest {
    @MockitoBean
    private ExecutedSynchronizations executedSynchronizations;
    private InitialSynchronizationProgress progress = mock(InitialSynchronizationProgress.class);

    @BeforeEach
    void setUp() throws SynchronizationNotExistsException {
        InitialSynchronization synchronization = Mockito.mock(InitialSynchronization.class);
        when(synchronization.getProgress()).thenReturn(progress);
        when(executedSynchronizations.getInitialSynchronization()).thenReturn(synchronization);
    }

    @Test
    @DisplayName("returns the corresponding synchronization object")
    void getSyncObject() throws Exception {
        var startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        when(progress.getStatus()).thenReturn(SynchronizationStatus.IN_PROGRESS);
        when(progress.getProgressInPercent()).thenReturn(75);
        when(progress.getStartTime()).thenReturn(startTime);

        mockMvc.perform(get("/synchronizations/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data.type", is("synchronization")))
               .andExpect(jsonPath("$.data.id", is(String.valueOf(1))))
               .andExpect(jsonPath("$.data.attributes.status", is(SynchronizationStatus.IN_PROGRESS.name())))
               .andExpect(jsonPath("$.data.attributes.progressPercent", is(75)))
               .andExpect(jsonPath("$.data.attributes.startTime", is(startTime.toString())))
               .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/synchronizations/1")));
    }

    @Test
    @DisplayName("returns 404 when the object is not found")
    void invalidId() throws Exception {
        when(executedSynchronizations.get(anyString())).thenThrow(new SynchronizationNotExistsException());
        mockMvc.perform(get("/synchronizations/0"))
               .andExpect(status().isNotFound());
    }
}
