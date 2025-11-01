package cloud.codestore.core.repositories.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The tag repository")
class TagRepositoryTest {
    private final TagRepository repository = new TagRepository();

    @Test
    @DisplayName("is empty after creation")
    void isEmpty() {
        assertThat(repository.read()).isEmpty();
    }

    @Test
    @DisplayName("adds single tags")
    void addSingleTag() {
        repository.create("tag");
        assertThat(repository.read()).hasSize(1);
    }

    @Test
    @DisplayName("adds multiple tags")
    void addMultipleTags() {
        repository.add(Set.of("A", "B", "C"));
        assertThat(repository.read()).hasSize(3);
    }

    @Test
    @DisplayName("only contains distinct tags")
    void distinctTags() {
        repository.add(List.of("A", "B", "C", "A", "B"));
        repository.create("C");
        assertThat(repository.read()).hasSize(3);
    }
}