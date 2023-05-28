package cloud.codestore.core.validation;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static cloud.codestore.core.validation.SnippetProperty.*;

public class SnippetValidator {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 10000;
    private static final int MAX_CODE_LENGTH = 10000;

    private final Map<SnippetProperty, String> validationMessages = new HashMap<>();

    public void validate(@Nonnull Snippet snippet) throws InvalidSnippetException {
        validateTitle(snippet.getTitle());
        validateDescription(snippet.getDescription());
        validateCode(snippet.getCode());

        if (!validationMessages.isEmpty())
            throw new InvalidSnippetException(validationMessages);
    }

    private void validateTitle(@Nonnull String title) {
        if (title.isBlank())
            validationMessages.put(TITLE, "invalidSnippet.title.missing");
        else if (title.length() > MAX_TITLE_LENGTH)
            validationMessages.put(TITLE, "invalidSnippet.title.invalidLength");
    }

    private void validateDescription(@Nonnull String description) {
        if (description.length() > MAX_DESCRIPTION_LENGTH)
            validationMessages.put(DESCRIPTION, "invalidSnippet.description.invalidLength");
    }

    private void validateCode(@Nonnull String code) {
        if (code.isBlank())
            validationMessages.put(CODE, "invalidSnippet.code.missing");
        else if (code.length() > MAX_CODE_LENGTH)
            validationMessages.put(CODE, "invalidSnippet.code.invalidLength");
    }
}
