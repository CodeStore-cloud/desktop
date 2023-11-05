package cloud.codestore.core.usecases.createtag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("The create-tag use case")
class CreateTagTest {
    private static final String TAG = "test-tag";

    @Mock
    private CreateTagQuery query;
    @Mock
    private TagValidator validator;
    private CreateTag useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTag(query, validator);
    }

    @Test
    @DisplayName("validates the tag before saving it")
    void validateAndSave() throws InvalidTagException {
        useCase.create(TAG);
        verify(validator).validate(TAG);
        verify(query).create(TAG);
    }
}