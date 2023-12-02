package cloud.codestore.core.application;

import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Creates a file which contains the access token to the {CodeStore} Core API after startup.
 */
@Component
class AccessTokenWriter {
    private final Directory binDirectory;
    private final String accessToken;

    AccessTokenWriter(
            @Qualifier("bin") Directory binDirectory,
            @Qualifier("accessToken") String accessToken
    ) {
        this.binDirectory = binDirectory;
        this.accessToken = accessToken;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void writeAccessToken() {
        File tokenFile = binDirectory.getFile("core-api-access-token");
        tokenFile.write(accessToken);
        tokenFile.path().toFile().deleteOnExit();
    }
}
