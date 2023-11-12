package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The list-snippets use case")
class ListSnippetsTest {
    @Mock
    private ReadSnippetsQuery query;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSnippets(query);
    }

    @Test
    @DisplayName("returns code snippets based on the filter properties")
    void returnAllSnippets() {
        var filter = new FilterProperties();
        var expectedResult = filteredSnippets();
        when(query.readSnippets(filter)).thenReturn(expectedResult);

        var snippets = useCase.list(filter);
        assertThat(snippets).isSameAs(expectedResult);
        verify(query).readSnippets(filter);
    }

    private List<Snippet> filteredSnippets() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> Snippet.builder().id(String.valueOf(id)).build())
                     .toList();
    }
}