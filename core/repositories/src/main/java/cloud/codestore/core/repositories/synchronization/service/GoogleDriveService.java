package cloud.codestore.core.repositories.synchronization.service;

import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.synchronization.RemoteDirectory;
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
public class GoogleDriveService {
    private static final String APPLICATION_NAME = "CodeStore Desktop Application";
    private static final String CREDENTIALS_FILE_PATH = "/google.drive.auth.json";
    private static final String ROOT_DIRECTORY_NAME = "CodeStore";

    private final Directory tokensDirectory;
    private JsonFactory jsonFactory;

    GoogleDriveService(@Qualifier("sync") Directory syncDirectory) {
        this.tokensDirectory = syncDirectory;
    }

    public RemoteDirectory login() {
        jsonFactory = GsonFactory.getDefaultInstance();
        Drive service = authenticate();
        return new GoogleDriveDirectory(service, ROOT_DIRECTORY_NAME);
    }

    private Drive authenticate() {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(httpTransport);
            return new Drive.Builder(httpTransport, jsonFactory, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException exception) {
            throw new RepositoryException("cloud.googleLoginFailed", exception);
        }
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        Objects.requireNonNull(in, "Resource not found: " + CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        List<String> scopes = Collections.singletonList(DriveScopes.DRIVE_FILE);
        GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory.path().toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().build();
        return new AuthorizationCodeInstalledApp(authFlow, receiver).authorize("user");
    }
}
