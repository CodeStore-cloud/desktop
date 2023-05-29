package cloud.codestore.core.api;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UriFactoryInitializer implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        UriFactory.init(8080);
    }
}
