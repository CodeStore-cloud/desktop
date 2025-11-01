package cloud.codestore.core.repositories.serialization;

import cloud.codestore.core.repositories.File;

import javax.annotation.Nonnull;

/**
 * A helper class for determining the file name of a snippet based on its ID, or vice versa.
 */
public final class SnippetFileHelper {
    private static final String JSON_FILE_EXTENSION = ".json";

    public static String getFileName(@Nonnull String snippetId) {
        return snippetId + JSON_FILE_EXTENSION;
    }

    public static String getSnippetId(@Nonnull String fileName) {
        return fileName.substring(0, fileName.length() - JSON_FILE_EXTENSION.length());
    }

    public static String getSnippetId(@Nonnull File file) {
        return getSnippetId(file.getName());
    }
}
