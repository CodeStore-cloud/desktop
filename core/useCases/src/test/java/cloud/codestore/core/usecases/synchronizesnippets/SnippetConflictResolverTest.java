package cloud.codestore.core.usecases.synchronizesnippets;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The snippet conflict resolver")
class SnippetConflictResolverTest {
    private static final String SNIPPET_ID = "123";
    private static final OffsetDateTime NOW = OffsetDateTime.now();

    @Spy
    private SnippetConflictResolver conflictResolver;

    @Test
    @DisplayName("applies item A when date A is after date B")
    void appliesItemAWhenDateAIsAfterDateB() throws Exception {
        String etagA = NOW.toString();
        String etagB = NOW.minusSeconds(1).toString();

        conflictResolver.resolve(SNIPPET_ID, etagA, etagB);

        verify(conflictResolver).applyItemA();
    }

    @Test
    @DisplayName("applies item B when date B is after date A")
    void appliesItemBWhenDateBIsAfterDateA() throws Exception {
        String etagA = NOW.minusSeconds(1).toString();
        String etagB = NOW.toString();

        conflictResolver.resolve(SNIPPET_ID, etagA, etagB);

        verify(conflictResolver).applyItemB();
    }

    @Test
    @DisplayName("does not apply any item when dates are equal")
    void doesNotApplyAnyItemWhenDatesAreEqual() throws Exception {
        conflictResolver.resolve(SNIPPET_ID, NOW.toString(), NOW.toString());

        verify(conflictResolver, never()).applyItemA();
        verify(conflictResolver, never()).applyItemB();
    }

    @Test
    @DisplayName("throws exception for invalid etag format")
    void throwsExceptionForInvalidEtagFormat() {
        Assertions.assertThatThrownBy(() -> conflictResolver.resolve(SNIPPET_ID, "not-a-date", NOW.toString()))
                          .isInstanceOf(DateTimeParseException.class);
    }
}
