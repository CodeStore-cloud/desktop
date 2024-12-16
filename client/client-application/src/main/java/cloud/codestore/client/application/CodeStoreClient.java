package cloud.codestore.client.application;

import cloud.codestore.client.UseCase;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.ui.FXMLLoaderFactory;
import cloud.codestore.client.ui.FxApplication;
import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * The main class of the {CodeStore} Client.
 */
@Configuration
@ComponentScan(
        basePackages = "cloud.codestore.client",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = {UseCase.class, FxController.class, Repository.class}
        )
)
@PropertySources({
        @PropertySource("classpath:/application.properties"),
        @PropertySource(value = "classpath:/application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class CodeStoreClient {
    private static final Logger LOGGER = LogManager.getLogger(CodeStoreClient.class);

    public static void main(String[] args) {
        LOGGER.info("Starting {CodeStore} client ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(CodeStoreClient.class);
        LOGGER.debug("Active profiles: {}", Arrays.toString(context.getEnvironment().getActiveProfiles()));
        FXMLLoaderFactory.setControllerFactory(context::getBean);
        Application.launch(FxApplication.class);
    }

    @Bean
    public HttpClient httpClient(@Value("${codestore.data:}") String dataDirectory) {
        Path directory = getDataDirectory(dataDirectory);
        LOGGER.debug("Data directory: {}", dataDirectory);
        String apiRootUrl = new RootUrlReader().readApiUrl(directory);
        String accessToken = new AccessTokenReader().readAccessToken(directory);
        LOGGER.info("Connecting to {CodeStore} Core at {}", apiRootUrl);
        return new HttpClient(apiRootUrl, accessToken);
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    /**
     * @return the directory where the binary executables of {CodeStore} are located.
     */
    @Bean
    public Path binDirectory(@Value("${codestore.bin:..}") String binaryPath) {
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
