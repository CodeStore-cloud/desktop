package cloud.codestore.core.application.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Provides functionality to check for updates and to update the application.
 */
@Component
class Updater {
    private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

    private final String homepageUrl;
    private final int currentVersion;
    private Path downloadedApplicationFile;

    Updater(
            @Value("${homepage.url}") String homepageUrl,
            @Value("${application.version}") String currentVersion
    ) {
        this.homepageUrl = homepageUrl;
        this.currentVersion = asInt(currentVersion);
    }

    /**
     * Checks if a new version is available.
     *
     * @return a {@link CompletableFuture} object that, when completed, contains the information if a new version
     * is available.
     */
    CompletableFuture<Boolean> isUpdateAvailable() {
        return WebClient.create(homepageUrl)
                        .get()
                        .uri("/download/latestVersion.json")
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(this::parseVersionInfo)
                        .map(this::checkVersion)
                        .onErrorResume(exception -> {
                            LOGGER.error("Failed to check for updates.", exception);
                            return Mono.just(false);
                        })
                        .toFuture();
    }

    /**
     * Downloads the new {CodeStore} version to a temporary directory.
     *
     * @throws IOException if the file could not be downloaded or saved.
     */
    void downloadNewVersion() throws IOException {
        Path tempDir = Files.createTempDirectory("codestore-update");
        downloadedApplicationFile = tempDir.resolve("CodeStore.exe");
        Files.deleteIfExists(downloadedApplicationFile);

        byte[] fileContent = downloadExe();
        Files.write(downloadedApplicationFile, fileContent);
        LOGGER.info("Downloaded new version to: {}", downloadedApplicationFile);
    }

    void installUpdate() throws IOException {
        if (downloadedApplicationFile != null && Files.exists(downloadedApplicationFile)) {
            try {
                LOGGER.info("Executing {}", downloadedApplicationFile);
                ProcessBuilder processBuilder = new ProcessBuilder(downloadedApplicationFile.toString());
                processBuilder.start();
            } catch (IOException exception) {
                LOGGER.error("Failed to execute {}", downloadedApplicationFile, exception);
                throw exception;
            }
        }
    }

    @Nullable
    Path getDownloadedApplicationFile() {
        return downloadedApplicationFile;
    }

    private byte[] downloadExe() throws IOException {
        try {
            return WebClient.create(homepageUrl)
                            .get()
                            .uri("/download/CodeStore.exe")
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .onErrorResume(exception -> {
                                LOGGER.error("Failed to check for updates.", exception);
                                return Mono.error(exception);
                            })
                            .block();
        } catch (Exception exception) {
            throw new IOException("Failed to download CodeStore.exe", exception);
        }
    }

    private int parseVersionInfo(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(body);
            return node.path("latestVersion").asInt(0);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean checkVersion(int latestVersion) {
        boolean updateAvailable = latestVersion > currentVersion;
        if (updateAvailable) {
            LOGGER.info("Checking for updates...");
        }
        return updateAvailable;
    }

    /**
     * Converts a version string (e.g. "1.2.3") into an integer (e.g. 1002003).
     * Each segment is zero-padded to 3 digits: "1.2.3" → 001.002.003 → 1002003.
     * <br/>
     * Version suffixes like {@code -SNAPSHOT} are ignored.
     *
     * @param version the version string
     * @return the parsed integer
     * @throws IllegalArgumentException if the version format is invalid
     */
    private int asInt(String version) {
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf("-"));
        }

        try {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);

            return major * 1_000_000 + minor * 1_000 + patch;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid version number " + version, exception);
        }
    }
}
