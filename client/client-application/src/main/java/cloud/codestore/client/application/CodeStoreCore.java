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
public class CodeStoreCore {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CodeStoreCore.class);
        FXMLLoaderFactory.setControllerFactory(context::getBean);
        Application.launch(FxApplication.class);
    }

    @Bean
    public HttpClient httpClient() {
        Path binDirectory = getBinDirectory();
        String apiRootUrl = new RootUrlReader().readApiUrl(binDirectory);
        String accessToken = new AccessTokenReader().readAccessToken(binDirectory);
        return new HttpClient(apiRootUrl, accessToken);
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    private Path getBinDirectory() {
        Path devFile = Path.of("bin");
        if (Files.exists(devFile)) {
            return devFile;
        }

        return Path.of("..");
    }
}
