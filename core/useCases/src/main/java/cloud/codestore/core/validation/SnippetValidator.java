package cloud.codestore.core.validation;

import cloud.codestore.core.Snippet;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SnippetValidator {
    private static final int MAX_TITLE_LENGTH = 100;

    private final Map<String, String> validationMessages = new HashMap<>();

    public void validate(@Nonnull Snippet snippet) throws InvalidSnippetException {
        validateTitle(snippet.getTitle());

        if (!validationMessages.isEmpty())
            throw new InvalidSnippetException(validationMessages);
    }

    private void validateTitle(@Nonnull String title) {
        if (title.isBlank())
            validationMessages.put("title", "invalidSnippet.title.missing");
        else if (title.length() > MAX_TITLE_LENGTH)
            validationMessages.put("title", "invalidSnippet.title.invalidLength");
    }
}
