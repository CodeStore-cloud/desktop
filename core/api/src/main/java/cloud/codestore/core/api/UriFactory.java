package cloud.codestore.core.api;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * A factory which constructs the URIs to the resources.
 */
@Component
public class UriFactory implements ApplicationListener<ServletWebServerInitializedEvent> {
    private static String ROOT_URI;

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        init(port);
    }

    /**
     * Initializes the {@link UriFactory} with the port of the local {CodeStore} Core server.
     *
     * @param port the port of the server.
     */
    public static void init(int port) {
        ROOT_URI = "http://localhost:" + port;
    }

    /**
     * @param path the path of a resource.
     * @return the absolute URI of the corresponding resource.
     */
    @Nonnull
    public static String createUri(String path) {
        return ROOT_URI + path;
    }
}
