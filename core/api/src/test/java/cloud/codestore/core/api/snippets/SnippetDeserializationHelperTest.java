package cloud.codestore.core.api.snippets;

import cloud.codestore.core.Language;
import cloud.codestore.core.api.languages.LanguageResource;
import cloud.codestore.core.usecases.readlanguage.LanguageNotExistsException;
import cloud.codestore.core.usecases.readlanguage.ReadLanguage;
import cloud.codestore.jsonapi.relationship.ToOneRelationship;
import cloud.codestore.jsonapi.resource.ResourceIdentifierObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The deserialization helper")
class SnippetDeserializationHelperTest {
    @Mock
    private ReadLanguage readLanguageUseCase;
    private SnippetDeserializationHelper deserializationHelper;

    @BeforeEach
    void setUp() {
        deserializationHelper = new SnippetDeserializationHelper(readLanguageUseCase);
    }

    @Nested
    @DisplayName("when reading a programming language")
    class GetLanguage {
        @Test
        @DisplayName("returns the related programming language")
        void returnLanguage() throws LanguageNotExistsException {
            var relationship = new ToOneRelationship<>().setData(id("1"));
            when(readLanguageUseCase.read(1)).thenReturn(Language.C);
            assertThat(deserializationHelper.getLanguage(relationship)).isEqualTo(Language.C);
        }

        @Test
        @DisplayName("throws a LanguageNotExistsException if the related ID is not an integer")
        void idNotInt() {
            var relationship = new ToOneRelationship<>().setData(id("a"));
            assertThatThrownBy(() -> deserializationHelper.getLanguage(relationship))
                    .isInstanceOf(LanguageNotExistsException.class);
        }

        @Test
        @DisplayName("returns null if relationship is null")
        void nullRelationship() throws LanguageNotExistsException {
            assertThat(deserializationHelper.getLanguage(null)).isNull();
        }

        @Test
        @DisplayName("returns null if relationship does not relate to a programming language")
        void nonLanguageRelationship() throws LanguageNotExistsException {
            var id = new ResourceIdentifierObject("tag", "1");
            var relationship = new ToOneRelationship<>().setData(id);
            assertThat(deserializationHelper.getLanguage(relationship)).isNull();
        }

        private ResourceIdentifierObject id(String id) {
            return new ResourceIdentifierObject(LanguageResource.RESOURCE_TYPE, id);
        }
    }
}