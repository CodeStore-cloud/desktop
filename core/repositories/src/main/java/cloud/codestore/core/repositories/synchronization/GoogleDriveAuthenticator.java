package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.RepositoryException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
class GoogleDriveAuthenticator {
    private static final String APPLICATION_NAME = "CodeStore Desktop Application";
    private static final String CREDENTIALS_FILE_PATH = "/google.drive.auth.json";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final Directory tokensDirectory;

    GoogleDriveAuthenticator(@Qualifier("sync") Directory syncDirectory) {
        this.tokensDirectory = syncDirectory;
    }

    Drive authenticate() {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(httpTransport);
            return new Drive.Builder(httpTransport, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException exception) {
            throw new RepositoryException("cloud.googleLoginFailed", exception);
        }
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        InputStream in = GoogleDriveSnippetSet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        Objects.requireNonNull(in, "Resource not found: " + CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory.path().toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().build();
        return new AuthorizationCodeInstalledApp(authFlow, receiver).authorize("user");
    }
}
