package cloud.codestore.core.application.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the latest available application.
 */
@Component
class LatestApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatestApplication.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient client = HttpClient.newHttpClient();
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
                            .thenApply(latestVersion -> {
                                boolean updateAvailable = latestVersion > currentVersionAsInt;
                                LOGGER.info(updateAvailable ? "Update available" : "Application up to date");
                                return updateAvailable;
                            });
    }

    /**
     * @return an {@link InstallerExecutable} that represents the executable for installing the latest application.
     */
    InstallerExecutable getInstaller() throws IOException {
        LOGGER.info("Downloading installer...");
        HttpRequest request = HttpRequest.newBuilder()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .uri(URI.create(homepageUrl + "/download/CodeStore.exe"))
                                         .timeout(DEFAULT_TIMEOUT)
                                         .GET()
                                         .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
                return new InstallerExecutable(contentLength, response.body());
            } else {
                throw new IOException("HTTP Error: " + response.statusCode());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException(exception);
        }
    }

    private CompletableFuture<Integer> loadLatestVersion() {
        HttpRequest request = HttpRequest.newBuilder()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .uri(URI.create(homepageUrl + "/download/latestVersion.json"))
                                         .timeout(DEFAULT_TIMEOUT)
                                         .GET()
                                         .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                     .thenApply(response -> {
                         if (response.statusCode() == 200) {
                             return parseVersionInfo(response.body());
                         } else {
                             throw new UncheckedIOException(new IOException("HTTP Error: " + response.statusCode()));
                         }
                     });
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
