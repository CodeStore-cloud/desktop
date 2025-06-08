package cloud.codestore.core.application.update;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The LatestApplication")
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

    /**
     * The context can´t be cleared as long as the context handler is sleeping.
     * So we need to wait for it to finish sleeping.
     */
    private void awaitSleepingContextHandler(CountDownLatch countDownLatch) throws InterruptedException {
        countDownLatch.await();
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

        @Test
        @DisplayName("returns false if a timeout occurs")
        void handleTimeout() throws Exception {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            context.setHandler(exchange -> {
                try {
                    Thread.sleep(6000);
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                    countDownLatch.countDown();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    countDownLatch.countDown();
                }
            });

            expectVersionAvailable(false);

            awaitSleepingContextHandler(countDownLatch);
        }

        private void expectVersionAvailable(boolean expectedResult) throws Exception {
            latestApplication.init();
            Boolean updateAvailable = latestApplication.isNewerThan("2.0.0").get();
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
    @DisplayName("for downloading the new version")
    class DownloadNewVersionTest {
        private static final byte[] FILE_CONTENT = "mock content".getBytes();

        @BeforeEach
        void setUp() {
            context = server.createContext("/download/CodeStore.exe");
        }

        @Test
        @DisplayName("creates an InstallerExecutable instance")
        void downloadsToTempDir() throws NoSuchFieldException, IOException {
            context.setHandler(returnOk());

            InstallerExecutable installer = latestApplication.getInstaller();
            assertThat(installer).isNotNull();
            assertThat(getContentLength(installer)).isEqualTo(FILE_CONTENT.length);

            try (InputStream in = getInputStream(installer); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                StreamUtils.copy(in, out);
                assertThat(out.toByteArray()).isEqualTo(FILE_CONTENT);
            }
        }

        @Test
        @DisplayName("fails if the server returns an error")
        void serverError() {
            context.setHandler(returnNotFound());
            assertThatThrownBy(() -> latestApplication.getInstaller()).isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("fails if a timeout occurs")
        void handleTimeout() throws Exception {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            context.setHandler(exchange -> {
                try {
                    Thread.sleep(6000);
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                    countDownLatch.countDown();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    countDownLatch.countDown();
                }
            });

            assertThatThrownBy(() -> latestApplication.getInstaller()).isInstanceOf(HttpTimeoutException.class);
            awaitSleepingContextHandler(countDownLatch);
        }

        private HttpHandler returnOk() {
            return exchange -> {
                exchange.sendResponseHeaders(200, FILE_CONTENT.length);
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(FILE_CONTENT.length));
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");

                try (OutputStream responseStream = exchange.getResponseBody()) {
                    responseStream.write(FILE_CONTENT);
                    responseStream.flush();
                }
            };
        }

        private InputStream getInputStream(InstallerExecutable installer) throws NoSuchFieldException {
            Field field = InstallerExecutable.class.getDeclaredField("inputStream");
            ReflectionUtils.makeAccessible(field);
            return (InputStream) ReflectionUtils.getField(field, installer);
        }

        private Long getContentLength(InstallerExecutable installer) throws NoSuchFieldException {
            Field field = InstallerExecutable.class.getDeclaredField("contentLength");
            ReflectionUtils.makeAccessible(field);
            return (Long) ReflectionUtils.getField(field, installer);
        }
    }
}