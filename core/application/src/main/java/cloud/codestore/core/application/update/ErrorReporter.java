package cloud.codestore.core.application.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sends an error report to the server.
 */
@Component
class ErrorReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorReporter.class);

    private final String reportUrl;
    private final String version;

    ErrorReporter(@Value("${homepage.url}") String homepageUrl, @Value("${application.version}") String version) {
        this.reportUrl = homepageUrl + "/error/";
        this.version = version;
    }

    void sendReport(Throwable throwable) {
        LOGGER.debug("Sending error report.");
        String report = generateReport(throwable);
        send(report);
    }

    private String generateReport(Throwable throwable) {
        String time = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now());
        String operatingSystem = System.getProperty("os.name");
        String error = stringify(throwable);

        return """
                ERROR REPORT
                ==================================================
                Time:    %s
                Version: %s
                OS:      %s
                ==================================================
                %s"""
                .formatted(time, version, operatingSystem, error);
    }

    private String stringify(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private void send(String report) {
        HttpRequest request = HttpRequest.newBuilder()
                                         .version(HttpClient.Version.HTTP_1_1)
                                         .uri(URI.create(reportUrl))
                                         .timeout(Duration.ofSeconds(5))
                                         .POST(HttpRequest.BodyPublishers.ofString(report))
                                         .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                  .thenApply(response -> {
                      int statusCode = response.statusCode();
                      if (statusCode != 200 && statusCode != 204) {
                          throw new UncheckedIOException(new IOException("HTTP Error: " + statusCode));
                      }
                      return null;
                  })
                  .exceptionally(exception -> {
                      LOGGER.error("Error when sending error report.", exception);
                      return null;
                  });
        }
    }
}
