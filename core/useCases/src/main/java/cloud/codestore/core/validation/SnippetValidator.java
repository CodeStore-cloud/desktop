package cloud.codestore.core.validation;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SnippetValidator {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 10000;
    private static final int MAX_CODE_LENGTH = 10000;

    private final Map<String, String> validationMessages = new HashMap<>();

    public void validate(@Nonnull Snippet snippet) throws InvalidSnippetException {
        validateTitle(snippet.getTitle());
        validateDescription(snippet.getDescription());
        validateCode(snippet.getCode());

        if (!validationMessages.isEmpty())
            throw new InvalidSnippetException(validationMessages);
    }

    private void validateTitle(@Nonnull String title) {
        if (title.isBlank())
            validationMessages.put("title", "invalidSnippet.title.missing");
        else if (title.length() > MAX_TITLE_LENGTH)
            validationMessages.put("title", "invalidSnippet.title.invalidLength");
    }

    private void validateDescription(@Nonnull String description) {
        if (description.length() > MAX_DESCRIPTION_LENGTH)
            validationMessages.put("description", "invalidSnippet.description.invalidLength");
    }

    private void validateCode(@Nonnull String code) {
        if (code.isBlank())
            validationMessages.put("code", "invalidSnippet.code.missing");
        else if (code.length() > MAX_CODE_LENGTH)
            validationMessages.put("code", "invalidSnippet.code.invalidLength");
    }
}
