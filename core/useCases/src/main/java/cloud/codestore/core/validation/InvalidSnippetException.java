package cloud.codestore.core.validation;

import cloud.codestore.core.CoreException;

import java.util.Map;

/**
 * Exception in case a code snippet that should be saved is invalid.
 */
public class InvalidSnippetException extends CoreException
{
    private final Map<SnippetProperty, String> validationMessages;
    
    InvalidSnippetException(Map<SnippetProperty, String> validationMessages)
    {
        super("error.invalidSnippet");
        this.validationMessages = validationMessages;
        for(var entry : validationMessages.entrySet())
            entry.setValue(createMessage(entry.getValue()));
    }
    
    /**
     * @return a map containing an error message for each invalid snippet property.
     * The keys of the map are the names of the properties, the values are the corresponding error message.
     */
    public Map<SnippetProperty, String> getValidationMessages()
    {
        return validationMessages;
    }
}
