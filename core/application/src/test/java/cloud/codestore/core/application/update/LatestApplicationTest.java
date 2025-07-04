package cloud.codestore.core.application.update;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The LatestApplication object")
class LatestApplicationTest {

    private static HttpServer server;
    private HttpContext context;
    private LatestApplication latestApplication;

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
        String url = "http://localhost:" + server.getAddress().getPort();
        latestApplication = new LatestApplication(url);
    }

    @AfterEach
    void tearDown() {
        server.removeContext(context);
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
            Boolean updateAvailable = latestApplication.isNewerThan("2.0.0")
                                                       .get(1, TimeUnit.SECONDS);
            assertThat(updateAvailable).isEqualTo(expectedResult);
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
            if (downloadedFile != null) {
                try {
                    Files.deleteIfExists(downloadedFile);
                    Files.deleteIfExists(downloadedFile.getParent());
                } catch (IOException ignore) {}
            }
        }

        @Test
        @DisplayName("saves the file to a temporary directory")
        void downloadsToTempDir() throws Exception {
            context.setHandler(returnOk());

            InstallerExecutable installer = latestApplication.download().get(1, TimeUnit.SECONDS);

            assertThat(installer).isNotNull();

            Field field = InstallerExecutable.class.getDeclaredField("file");
            ReflectionUtils.makeAccessible(field);
            downloadedFile = (Path) ReflectionUtils.getField(field, installer);
            assertThat(downloadedFile).isNotNull()
                                      .hasFileName("CodeStore.exe")
                                      .hasBinaryContent(FILE_CONTENT);
        }

        @Test
        @DisplayName("fails if the server returns an error")
        void serverError() {
            context.setHandler(returnNotFound());

            assertThatThrownBy(() -> latestApplication.download().get())
                    .rootCause()
                    .isInstanceOf(WebClientResponseException.class);
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