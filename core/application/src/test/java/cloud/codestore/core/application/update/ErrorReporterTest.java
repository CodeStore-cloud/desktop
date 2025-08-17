package cloud.codestore.core.application.update;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The error reporter")
class ErrorReporterTest {
    private HttpServer server;
    private ErrorReporter errorReporter;
    private String receivedErrorReport;
    private CountDownLatch asyncRequestCountdownLatch;

    @BeforeEach
    void setUp() throws IOException {
        asyncRequestCountdownLatch =  new CountDownLatch(1);
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
        server.createContext("/error", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            receivedErrorReport = new String(requestBody, StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(204, 0);
            exchange.getResponseBody().close();
            asyncRequestCountdownLatch.countDown();
        });

        String url = "http://localhost:" + server.getAddress().getPort();
        errorReporter = new ErrorReporter(url, "2.0.0");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("sends an error report to <homepage>/error")
    void sendReports() throws InterruptedException {
        Exception exception = new IOException("Something went wrong...");
        errorReporter.sendReport(exception);
        asyncRequestCountdownLatch.await();

        assertThat(receivedErrorReport).startsWith("""
                ERROR REPORT
                ==================================================""");
        assertThat(receivedErrorReport).contains("Version: 2.0.0");
        assertThat(receivedErrorReport).containsIgnoringWhitespaces("""
                ==================================================
                java.io.IOException: Something went wrong...
                	at cloud.codestore.core.application.update.ErrorReporterTest.sendReports(ErrorReporterTest.java:48)
                	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
                	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
                	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)""");
    }
}