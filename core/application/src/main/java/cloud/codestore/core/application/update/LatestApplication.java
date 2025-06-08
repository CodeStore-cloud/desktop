package cloud.codestore.core.application.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the latest available application on the server.
 */
@Component
class LatestApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatestApplication.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final String homepageUrl;
    private CompletableFuture<Integer> latestVersion;

    LatestApplication(@Value("${homepage.url}") String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    @PostConstruct
    void init() {
        latestVersion = loadLatestVersion();
    }

    /**
     * Checks whether the version of the latest application is greater than the given one.
     *
     * @param currentVersion the current version of the application.
     * @return a {@link CompletableFuture} that, when completed, returns whether a new version is available.
     */
    CompletableFuture<Boolean> isNewerThan(String currentVersion) {
        int currentVersionAsInt = asInt(currentVersion);
        return latestVersion.exceptionally(exception -> {
                                LOGGER.error("Failed to check for updates.", exception);
                                return 0;
                            })
                            .thenApply(latestVersion -> latestVersion > currentVersionAsInt);
    }

    /**
     * Downloads the executable to install the latest application.
     *
     * @return a {@link CompletableFuture} that, when completed, returns an {@link InstallerExecutable} for installing
     * the application.
     */
    CompletableFuture<InstallerExecutable> download() {
        return downloadExe().thenApply(fileContent -> {
            try {
                return new InstallerExecutable(fileContent);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to save installer.", exception);
            }
        });
    }

    private CompletableFuture<byte[]> downloadExe() {
        return WebClient.create(homepageUrl)
                        .get()
                        .uri("/download/CodeStore.exe")
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .timeout(DEFAULT_TIMEOUT)
                        .toFuture();
    }

    private CompletableFuture<Integer> loadLatestVersion() {
        return WebClient.create(homepageUrl)
                        .get()
                        .uri("/download/latestVersion.json")
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(DEFAULT_TIMEOUT)
                        .map(this::parseVersionInfo)
                        .toFuture();
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
