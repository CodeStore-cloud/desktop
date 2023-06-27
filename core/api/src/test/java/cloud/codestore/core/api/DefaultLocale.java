package cloud.codestore.core.api;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

public class DefaultLocale implements BeforeAllCallback
{
    @Override
    public void beforeAll(ExtensionContext extensionContext)
    {
        Locale.setDefault(Locale.ENGLISH);
    }
}
