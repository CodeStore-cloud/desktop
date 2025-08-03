package cloud.codestore.core.application.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the Windows executable to install the application.
 * This class handles downloading and saving the file.
 */
class InstallerExecutable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerExecutable.class);
    private static final int BUFFER_SIZE = 524288; // 512 KB

    private final long contentLength;
    private final InputStream inputStream;
    private Path file;
    private ProgressListener progressListener = progress -> {};
    private boolean canceled;

    InstallerExecutable(long contentLength, @Nonnull InputStream inputStream) {
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }

    /**
     * Downloads the installer from the server.
     * @throws IOException if the file could not be downloaded or saved on the local file system.
     */
    void download() throws IOException {
        Path tempDir = Files.createTempDirectory("codestore-update");
        file = tempDir.resolve("CodeStore.exe");

        try (BufferedInputStream in = new BufferedInputStream(inputStream);
             BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file.toFile()))
        ) {
            double totalBytesRead = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bufferedBytes;

            while(!canceled && (bufferedBytes = in.read(buffer)) != -1)
            {
                fileStream.write(buffer, 0, bufferedBytes);
                totalBytesRead += bufferedBytes;
                progressListener.onProgress(contentLength > 0 ? totalBytesRead / contentLength : 0);
            }

            fileStream.flush();
        }

        if(canceled) {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Executes the downloaded installer.
     * @throws IOException if the file could not be executed.
     */
    void execute() throws IOException {
        if (file != null && Files.exists(file)) {
            try {
                LOGGER.info("Executing {}", file);
                ProcessBuilder processBuilder = new ProcessBuilder(file.toString());
                processBuilder.start();
            } catch (IOException exception) {
                LOGGER.error("Failed to execute {}", file, exception);
                throw exception;
            }
        }
    }

    void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    void cancelDownload() {
        canceled = true;
    }

    /**
     * A progress listener that is called as soon as a part of the file was downloaded.
     */
    interface ProgressListener {
        /**
         * @param progress the progress of the download between 0 and 1
         */
        void onProgress(double progress);
    }
}
