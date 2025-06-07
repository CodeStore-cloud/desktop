package cloud.codestore.core.application.update;


import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The Updater")
class UpdaterTest {
    private static final String CURRENT_VERSION = "2.0.0";

    private static HttpServer server;

    private Updater updater;
    private HttpContext context;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.stop(0);
    }

    @BeforeEach
    void setUp() {
        updater = new Updater(
                "http://localhost:" + server.getAddress().getPort(),
                CURRENT_VERSION
        );
    }

    private HttpHandler returnNotFound() {
        return exchange -> {
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().close();
        };
    }

    @Nested
    @DisplayName("when checking for updates")
    class LatestVersionTest {
        @BeforeEach
        void setUp() {
            context = server.createContext("/download/latestVersion.json");
        }

        @AfterEach
        void tearDown() {
            server.removeContext(context);
        }

        @Test
        @DisplayName("returns true if the latest version is greater than the current version")
        void newerVersion() throws Exception {
            context.setHandler(returnOk("{\"latestVersion\":2000001}"));
            expectVersionAvailable(true);
        }

        @Test
        @DisplayName("returns false if the latest version is smaller than the current version")
        void olderVersion() throws Exception {
            context.setHandler(returnOk("{\"latestVersion\":1999999}"));
            expectVersionAvailable(false);
        }

        @Test
        @DisplayName("returns false if the latest version is equal to the current version")
        void sameVersion() throws Exception {
            context.setHandler(returnOk("{\"latestVersion\":2000000}"));
            expectVersionAvailable(false);
        }

        @Test
        @DisplayName("returns false if the server returns an error")
        void serverError() throws Exception {
            context.setHandler(returnNotFound());
            expectVersionAvailable(false);
        }

        @Test
        @DisplayName("returns false if the JSON object cannot be parsed")
        void invalidJson() throws Exception {
            context.setHandler(returnOk("{invalidJson}"));
            expectVersionAvailable(false);
        }

        @Test
        @DisplayName("returns false if the version number cannot be parsed as integer")
        void invalidVersionNumber() throws Exception {
            context.setHandler(returnOk("{\"latestVersion\":\"notAnInteger\"}"));
            expectVersionAvailable(false);
        }

        private void expectVersionAvailable(boolean expectedResult) throws Exception {
            assertThat(updater.isUpdateAvailable().get(1, TimeUnit.SECONDS)).isEqualTo(expectedResult);
        }

        private HttpHandler returnOk(String body) {
            return exchange -> {
                byte[] responseBody = body.getBytes();

                exchange.sendResponseHeaders(200, responseBody.length);
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                try (OutputStream responseStream = exchange.getResponseBody()) {
                    responseStream.write(responseBody);
                    responseStream.flush();
                }
            };
        }
    }

    @Nested
    @DisplayName("when downloading the new version")
    class DownloadNewVersionTest {
        private static final byte[] FILE_CONTENT = "mock content".getBytes();
        private Path downloadedFile;

        @BeforeEach
        void setUp() {
            context = server.createContext("/download/CodeStore.exe");
        }

        @AfterEach
        void tearDown() {
            server.removeContext(context);
            if (downloadedFile != null) {
                try {
                    Files.deleteIfExists(downloadedFile);
                    Files.deleteIfExists(downloadedFile.getParent());
                } catch (IOException ignore) {}
            }
        }

        @Test
        @DisplayName("saves the file to a temporary directory")
        void downloadsToTempDir() throws IOException {
            context.setHandler(returnOk());

            updater.downloadNewVersion();

            downloadedFile = updater.getDownloadedApplicationFile();
            assertThat(downloadedFile).isNotNull();
            assertThat(downloadedFile).exists();
            assertThat(downloadedFile).isRegularFile();
            assertThat(downloadedFile).hasFileName("CodeStore.exe");
            assertThat(downloadedFile).hasBinaryContent(FILE_CONTENT);
        }

        @Test
        @DisplayName("fails if the server returns an error")
        void serverError() {
            context.setHandler(returnNotFound());

            assertThatThrownBy(() -> updater.downloadNewVersion())
                    .isInstanceOf(IOException.class)
                    .hasMessage("Failed to download CodeStore.exe");
        }

        private HttpHandler returnOk() {
            return exchange -> {
                exchange.sendResponseHeaders(200, FILE_CONTENT.length);
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");

                try (OutputStream responseStream = exchange.getResponseBody()) {
                    responseStream.write(FILE_CONTENT);
                    responseStream.flush();
                }
            };
        }
    }
}
