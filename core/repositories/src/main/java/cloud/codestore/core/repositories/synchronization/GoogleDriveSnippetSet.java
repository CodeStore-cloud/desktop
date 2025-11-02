package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.repositories.RepositoryException;
import cloud.codestore.core.repositories.serialization.SnippetFileHelper;
import cloud.codestore.core.repositories.serialization.SnippetReader;
import cloud.codestore.core.repositories.serialization.SnippetWriter;
import cloud.codestore.synchronization.ItemSet;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Represents the set of code snippets on the remote system. Usually a cloud storage.
 */
class GoogleDriveSnippetSet implements ItemSet<Snippet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveSnippetSet.class);
    private static final String CONTENT_TYPE_JSON = "application/json";

    private final SnippetReader snippetReader;
    private final SnippetWriter snippetWriter;
    private final Drive service;
    private File googleDriveSnippetsFolder;
    private Map<String, File> remoteSnippets;

    GoogleDriveSnippetSet(
            GoogleDriveAuthenticator authenticator,
            SnippetReader snippetReader,
            SnippetWriter snippetWriter
    ) {
        this.snippetReader = snippetReader;
        this.snippetWriter = snippetWriter;
        service = authenticator.authenticate();
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
            throw new RepositoryException("cloud.couldNotListFiles", exception);
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
        String content = readFile(remoteFile);
        return snippetReader.read(snippetId, content);
    }

    @Override
    public void addItem(String snippetId, Snippet snippet) throws Exception {
        LOGGER.debug("Create {} on remote system.", snippetId);
        String content = snippetWriter.stringify(snippet);
        String fileName = SnippetFileHelper.getFileName(snippetId);
        createFile(fileName, googleDriveSnippetsFolder, content, snippet.getCreated(),
                snippet.getOptionalModified().orElse(snippet.getCreated()));
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
        Objects.requireNonNull(snippet.getModified(), "Modified snippet has no 'modified' timestamp.");

        File file = remoteSnippets.get(snippetId);
        String content = snippetWriter.stringify(snippet);
        updateFile(file, snippet.getModified(), content);
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

        Map<String, File> snippetMap = new HashMap<>();
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
            if (files != null && !files.isEmpty()) {
                for (File file : files) {
                    String snippetId = SnippetFileHelper.getSnippetId(file.getName());
                    snippetMap.put(snippetId, file);
                }
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return snippetMap;
    }

    private String readFile(File file) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        service.files()
               .get(file.getId())
               .executeMediaAndDownloadTo(stream);

        return stream.toString(StandardCharsets.UTF_8);
    }

    private void createFile(
            String fileName, File parentFolder, String content, OffsetDateTime created, OffsetDateTime modified
    ) throws IOException {
        File fileMetadata = new File().setName(fileName)
                                      .setParents(Collections.singletonList(parentFolder.getId()))
                                      .setMimeType(CONTENT_TYPE_JSON)
                                      .setCreatedTime(toDateTime(created))
                                      .setModifiedTime(toDateTime(modified));

        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE_JSON,
                content.getBytes(StandardCharsets.UTF_8)
        );

        service.files()
               .create(fileMetadata, mediaContent)
               .execute();
    }

    private void updateFile(File file, OffsetDateTime modified, String content) throws IOException {
        File fileMetadata = new File().setModifiedTime(toDateTime(modified));
        AbstractInputStreamContent mediaContent = new ByteArrayContent(
                CONTENT_TYPE_JSON,
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

    @Nullable
    private DateTime toDateTime(@Nullable OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }

        TimeZone timeZone = TimeZone.getTimeZone(offsetDateTime.toZonedDateTime().getZone());
        return new DateTime(new Date(offsetDateTime.toInstant().toEpochMilli()), timeZone);
    }
}
