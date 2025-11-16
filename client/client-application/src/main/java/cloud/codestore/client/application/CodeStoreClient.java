package cloud.codestore.client.application;

import cloud.codestore.client.Injectable;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.ui.ApplicationReadyEvent;
import cloud.codestore.client.ui.FXMLLoaderFactory;
import cloud.codestore.client.ui.FxApplication;
import com.google.common.eventbus.EventBus;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * The main class of the {CodeStore} Client.
 */
@Configuration
@ComponentScan(
        basePackages = "cloud.codestore.client",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = {Injectable.class}
        )
)
@PropertySources({
        @PropertySource("classpath:/application.properties"),
        @PropertySource(value = "classpath:/application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class CodeStoreClient {
    private static final Logger LOGGER = LogManager.getLogger(CodeStoreClient.class);
    private static CompletableFuture<Void> uiInitialized =  new CompletableFuture<>();

    public static void main(String[] args) {
        LOGGER.info("Starting {CodeStore} client ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(CodeStoreClient.class);
        LOGGER.debug("Active profiles: {}", Arrays.toString(context.getEnvironment().getActiveProfiles()));
        FXMLLoaderFactory.setControllerFactory(context::getBean);
        FxApplication.setUiInitializedCallback(uiInitialized);
        Application.launch(FxApplication.class);
    }

    /**
     * Creates an {@link HttpClient} that
     *
     * @param dataDirectory the path of the directory containing the user´s data.
     * @return a {@link HttpClient} object that may not be fully initialized.
     */
    @Bean
    public HttpClient httpClient(@Value("${codestore.data:}") String dataDirectory) {
        Path directory = getDataDirectory(dataDirectory);
        LOGGER.debug("Data directory: {}", dataDirectory);

        AsyncFileReader fileReader = new AsyncFileReader();
        CompletableFuture<String> apiRootUrl = fileReader.readFile(directory.resolve("core-api-url"));
        CompletableFuture<String> accessToken = fileReader.readFile(directory.resolve("core-api-access-token"));

        CompletableFuture<Void> clientInitialized = new CompletableFuture<>();
        HttpClient httpClient = new HttpClient(apiRootUrl, accessToken, clientInitialized);

        CompletableFuture.allOf(uiInitialized, clientInitialized).thenRun(() -> {
            LOGGER.info("Connecting to {CodeStore} Core at {}", apiRootUrl.join());
            LOGGER.info("Application ready!");
            Platform.runLater(() -> eventBus().post(new ApplicationReadyEvent()));
        });

        return httpClient;
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    /**
     * @return the directory where the binary executables of {CodeStore} are located.
     */
    @Bean
    public Path binDirectory(@Value("${codestore.bin:.}") String binaryPath) {
        return Path.of(binaryPath).toAbsolutePath();
    }

    private Path getDataDirectory(String dataDirectory) {
        if (dataDirectory.isEmpty()) {
            String userHome = System.getProperty("user.home");
            return Paths.get(userHome, "CodeStore").toAbsolutePath();
        }

        return Path.of(dataDirectory).toAbsolutePath();
    }
}
