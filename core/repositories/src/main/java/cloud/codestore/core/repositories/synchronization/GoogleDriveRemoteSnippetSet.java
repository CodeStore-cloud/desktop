package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.snippets.SnippetFileHelper;
import cloud.codestore.synchronization.ItemSet;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Represents the set of code snippets on the remote system. Usually a cloud storage.
 */
class GoogleDriveRemoteSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveRemoteSnippetSet.class);
    private static final String APPLICATION_NAME = "CodeStore Desktop Application";
    private static final String CREDENTIALS_FILE_PATH = "/google.drive.auth.json";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CONTENT_TYPE = "application/json";

    private final Drive service;
    private File googleDriveSnippetsFolder;
    private Map<String, File> remoteSnippets;

    GoogleDriveRemoteSnippetSet(@Nonnull Directory tokensDirectory) {
        service = createGoogleDriveService(tokensDirectory);
    }

    private static Drive createGoogleDriveService(Directory tokensDirectory) {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(httpTransport, tokensDirectory);
            return new Drive.Builder(httpTransport, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException exception) {
            throw new RepositoryException("Login to Google Drive failed.", exception);
        }
    }

    private static Credential getCredentials(
            NetHttpTransport httpTransport,
            Directory tokensDirectory
    ) throws IOException {
        InputStream in = GoogleDriveRemoteSnippetSet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

    @Override
    public Set<String> getItemIds() {
        try {
            Optional<File> optional = getFolder("CodeStore", "root");
            File codestoreFolder = optional.isPresent() ? optional.get() : createFolder("CodeStore", "root");

            optional = getFolder("snippets", codestoreFolder.getId());
            googleDriveSnippetsFolder = optional.isPresent() ? optional.get() : createFolder("snippets", codestoreFolder.getId());
            remoteSnippets = listSnippetsInFolder(googleDriveSnippetsFolder);
        } catch (IOException exception) {
            throw new RepositoryException("Failed to list snippets on remote system.", exception);
        }
        return remoteSnippets.keySet();
    }

    @Override
    public boolean contains(String snippetId) {
        return remoteSnippets.containsKey(snippetId);
    }

    @Override
    public String getEtag(String snippetId) {
        File file = remoteSnippets.get(snippetId);
        return file.getModifiedTime().toStringRfc3339();
    }

    @Override
    public Snippet getItem(String snippetId) throws Exception {
        File remoteFile = remoteSnippets.get(snippetId);
        String content = readFile(remoteFile.getId());
        //TODO return Snippet/Content
        return null;
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Create {} on remote system.", snippetId);

        String content = ""; //TODO content
        String fileName = SnippetFileHelper.getFileName(snippetId);
        createFile(fileName, googleDriveSnippetsFolder, content);
    }

    @Override
    public void delete(String snippetId) throws Exception {
        LOGGER.debug("Delete {} on remote system.", snippetId);
        File file = remoteSnippets.get(snippetId);
        deleteFile(file);
    }

    @Override
    public void updateItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Update {} on remote system.", snippetId);

        File file = remoteSnippets.get(snippetId);
        String content = ""; //TODO content
        updateFile(file, content);
    }

    private Optional<File> getFolder(String folderName, String parentFolderId) throws IOException {
        String query = "mimeType = 'application/vnd.google-apps.folder' " +
                       "and name = '" + folderName + "' " +
                       "and '" + parentFolderId + "' in parents " +
                       "and trashed = false";

        FileList result = service.files()
                                 .list()
                                 .setQ(query)
                                 .setSpaces("drive")
                                 .setFields("files(id)")
                                 .execute();

        List<File> folders = result.getFiles();
        return folders == null || folders.isEmpty() ? Optional.empty() : Optional.of(folders.getFirst());
    }

    private File createFolder(String folderName, String parentFolderId) throws IOException {
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        folderMetadata.setParents(Collections.singletonList(parentFolderId));

        return service.files()
                      .create(folderMetadata)
                      .setFields("id")
                      .execute();
    }

    private Map<String, File> listSnippetsInFolder(File folder) throws IOException {
        String query = "mimeType != 'application/vnd.google-apps.folder' " +
                       "and '" + folder.getId() + "' in parents " +
                       "and trashed = false";

        Map<String, File> snippetMap = Collections.emptyMap();
        String pageToken = null;
        do {
            FileList result = service.files()
                                     .list()
                                     .setQ(query)
                                     .setSpaces("drive")
                                     .setFields("nextPageToken, files(id, name, modifiedTime)")
                                     .setPageToken(pageToken)
                                     .execute();

            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                //TODO folder not exists - create?
                System.out.println("No files found.");
            } else {
                snippetMap = new HashMap<>(files.size());
                for (File file : files) {
                    String snippetId = SnippetFileHelper.getSnippetId(file.getName());
                    snippetMap.put(snippetId, file);
                }
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return snippetMap;
    }

    private String readFile(String fileId) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        service.files()
               .get(fileId)
               .executeMediaAndDownloadTo(stream);

        return stream.toString(StandardCharsets.UTF_8);
    }

    private void createFile(String fileName, File parentFolder, String content) throws IOException {
        File fileMetadata = new File().setName(fileName)
                                      .setParents(Collections.singletonList(parentFolder.getId()))
                                      .setMimeType(CONTENT_TYPE)
                                      .setCreatedTime(null) //TODO creation time
                                      .setModifiedTime(null); //TODO modified time

        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE,
                content.getBytes(StandardCharsets.UTF_8)
        );

        service.files()
               .create(fileMetadata, mediaContent)
               .execute();
    }

    private void updateFile(File file, String content) throws IOException {
        File fileMetadata = new File().setModifiedTime(null); //TODO modified time
        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE,
                content.getBytes(StandardCharsets.UTF_8)
        );

        service.files()
               .update(file.getId(), fileMetadata, mediaContent)
               .execute();
    }

    private void deleteFile(File file) throws IOException {
        service.files()
               .delete(file.getId())
               .execute();
    }
}
