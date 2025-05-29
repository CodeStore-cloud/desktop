package cloud.codestore.core.application;

import cloud.codestore.core.api.UriFactory;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Creates a file which contains the root URL of the {CodeStore} Core API after startup.
 */
@Component
class RootUrlWriter {
    private final Directory dataDirectory;

    RootUrlWriter(@Qualifier("data") Directory dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @EventListener(ApplicationReadyEvent.class)
    void writeRootUrl() {
        String rootUrl = UriFactory.createUri("");
        File rootUrlFile = dataDirectory.getFile("core-api-url");
        rootUrlFile.write(rootUrl);
        rootUrlFile.path().toFile().deleteOnExit();
    }
}
