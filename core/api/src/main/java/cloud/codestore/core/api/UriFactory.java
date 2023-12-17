package cloud.codestore.core.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A factory which constructs the URIs to the resources.
 */
@Component
public class UriFactory implements ApplicationListener<ServletWebServerInitializedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriFactory.class);
    private static String ROOT_URI;

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        ROOT_URI = "http://localhost:" + port;
        LOGGER.info("Listening on " + ROOT_URI);
    }

    /**
     * @param path the path of a resource.
     * @return the absolute URI of the corresponding resource.
     */
    @Nonnull
    public static String createUri(@Nonnull String path) {
        return ROOT_URI + path;
    }

    /**
     * @param path          the path of a resource.
     * @param urlParameters one or more query parameters to be added.
     * @return the absolute URI of the corresponding resource.
     */
    @Nonnull
    public static String createUri(@Nonnull String path, @Nonnull Map<String, Object> urlParameters) {
        String querySegment = "";
        if (!urlParameters.isEmpty()) {
            querySegment += "?";
            querySegment += urlParameters.entrySet()
                                         .stream()
                                         .filter(entry -> entry.getValue() != null)
                                         .map(entry -> "%s=%s".formatted(encode(entry.getKey()), encode(entry.getValue())))
                                         .collect(Collectors.joining("&"));
        }

        return ROOT_URI + path + querySegment;
    }

    private static String encode(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
