package cloud.codestore.core.api;

import javax.annotation.Nonnull;

/**
 * A factory which constructs the URIs to the resources.
 */
public class UriFactory {
    private static String ROOT_URI;

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
