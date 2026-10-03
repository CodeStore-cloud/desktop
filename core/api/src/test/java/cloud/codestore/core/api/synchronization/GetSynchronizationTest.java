package cloud.codestore.core.api.synchronization;

import cloud.codestore.core.api.AbstractControllerTest;
import cloud.codestore.core.usecases.synchronizesnippets.*;
import cloud.codestore.jsonapi.document.JsonApiDocument;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SynchronizationController.class)
@Import(SynchronizationController.class)
@DisplayName("GET /synchronizations/{id}")
class GetSynchronizationTest extends AbstractControllerTest {
    private static final String SNIPPET_ID = "123";

    @MockitoBean
    private SnippetSynchronizations snippetSynchronizations;
    @MockitoBean
    private SynchronizationProcess synchronization;
    private SynchronizationProgress progress = mock(SynchronizationProgress.class);
    private SynchronizationState state = mock(SynchronizationState.class);

    @BeforeEach
    void setUp() {
        when(synchronization.getProgress()).thenReturn(progress);
        when(synchronization.getState()).thenReturn(state);
    }

    @Test
    @DisplayName("returns the global synchronization process")
    void getSyncProcess() throws Exception {
        var startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
        when(state.getStatus()).thenReturn(SynchronizationStatus.IN_PROGRESS);
        when(progress.getProgressInPercent()).thenReturn(75);
        when(state.getStartTime()).thenReturn(startTime);

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
    @DisplayName("returns the synchronization state for the corresponding code snippet")
    void getSnippetSyncState() throws Exception {
        SnippetSynchronizationState syncState = mock(SnippetSynchronizationState.class);
        when(snippetSynchronizations.get(SNIPPET_ID)).thenReturn(syncState);
        when(syncState.getSnippetId()).thenReturn(SNIPPET_ID);
        when(syncState.getStatus()).thenReturn(SynchronizationStatus.IN_PROGRESS);
        var startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
        when(syncState.getStartTime()).thenReturn(startTime);

        mockMvc.perform(get("/synchronizations/" + SNIPPET_ID))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data.type", is("snippet-synchronization")))
               .andExpect(jsonPath("$.data.id", is(SNIPPET_ID)))
               .andExpect(jsonPath("$.data.attributes.status", is(SynchronizationStatus.IN_PROGRESS.name())))
               .andExpect(jsonPath("$.data.attributes.startTime", is(startTime.toString())))
               .andExpect(jsonPath("$.data.attributes.error", is(IsNull.nullValue())))
               .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/synchronizations/" + SNIPPET_ID)));
    }

    @Test
    @DisplayName("returns the synchronization state for failed snippet-synchronizations")
    void getFailedSyncState() throws Exception {
        SnippetSynchronizationState syncState = mock(SnippetSynchronizationState.class);
        when(snippetSynchronizations.get(SNIPPET_ID)).thenReturn(syncState);
        when(syncState.getSnippetId()).thenReturn(SNIPPET_ID);
        when(syncState.getStatus()).thenReturn(SynchronizationStatus.FAILED);
        var startTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
        var endTime = startTime.plusSeconds(3);
        when(syncState.getStartTime()).thenReturn(startTime);
        when(syncState.getEndTime()).thenReturn(endTime);
        String errorMessage = "Error message";
        when(syncState.getError()).thenReturn(new Exception(errorMessage));

        mockMvc.perform(get("/synchronizations/" + SNIPPET_ID))
               .andExpect(status().isOk())
               .andExpect(content().contentType(JsonApiDocument.MEDIA_TYPE))
               .andExpect(jsonPath("$.data.type", is("snippet-synchronization")))
               .andExpect(jsonPath("$.data.id", is(SNIPPET_ID)))
               .andExpect(jsonPath("$.data.attributes.status", is(SynchronizationStatus.FAILED.name())))
               .andExpect(jsonPath("$.data.attributes.startTime", is(startTime.toString())))
               .andExpect(jsonPath("$.data.attributes.endTime", is(endTime.toString())))
               .andExpect(jsonPath("$.data.attributes.error.code", is("SYNCHRONIZATION_FAILED")))
               .andExpect(jsonPath("$.data.attributes.error.title", is("Synchronization Failed")))
               .andExpect(jsonPath("$.data.attributes.error.detail", is(errorMessage)))
               .andExpect(jsonPath("$.data.links.self", is("http://localhost:8080/synchronizations/" + SNIPPET_ID)));
    }

    @Test
    @DisplayName("returns 404 when the object is not found")
    void invalidId() throws Exception {
        when(snippetSynchronizations.get(anyString())).thenThrow(new SynchronizationNotExistsException());
        mockMvc.perform(get("/synchronizations/0"))
               .andExpect(status().isNotFound());
    }
}
