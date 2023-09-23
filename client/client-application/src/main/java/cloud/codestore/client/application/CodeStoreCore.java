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
        return new HttpClient("http://localhost:53232");
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
}
