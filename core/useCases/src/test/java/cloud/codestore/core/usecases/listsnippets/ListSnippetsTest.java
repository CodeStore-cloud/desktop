package cloud.codestore.core.usecases.listsnippets;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.SnippetBuilder;
import cloud.codestore.core.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The list-snippets use case")
class ListSnippetsTest {
    @Mock
    private SnippetRepository repository;
    private ListSnippets useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSnippets(repository);
    }

    @Test
    @DisplayName("returns all available code snippets")
    void returnAllSnippets() {
        var expectedResult = allSnippets();
        when(repository.get()).thenReturn(expectedResult);

        var snippets = useCase.list();
        assertThat(snippets).isSameAs(expectedResult);
    }

    private List<Snippet> allSnippets() {
        return Stream.of(1, 2, 3, 4, 5)
                     .map(id -> new SnippetBuilder().id(String.valueOf(id)).build())
                     .toList();
    }
}