package cloud.codestore.client.application;

import cloud.codestore.client.UseCase;
import cloud.codestore.client.repositories.HttpClient;
import cloud.codestore.client.repositories.Repository;
import cloud.codestore.client.ui.FXMLLoaderFactory;
import cloud.codestore.client.ui.FxApplication;
import cloud.codestore.client.ui.FxController;
import com.google.common.eventbus.EventBus;
import javafx.application.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
public class CodeStoreClient {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CodeStoreClient.class);
        FXMLLoaderFactory.setControllerFactory(context::getBean);
        Application.launch(FxApplication.class);
    }

    @Bean
    public HttpClient httpClient() {
        Path directory = getDataDirectory();
        String apiRootUrl = new RootUrlReader().readApiUrl(directory);
        String accessToken = new AccessTokenReader().readAccessToken(directory);
        return new HttpClient(apiRootUrl, accessToken);
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    private Path getDataDirectory() {
        Path devPath = Path.of("data");
        if (Files.exists(devPath)) {
            return devPath;
        }

        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "CodeStore").toAbsolutePath();
    }
}
