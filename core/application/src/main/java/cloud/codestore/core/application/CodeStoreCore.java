package cloud.codestore.core.application;

import cloud.codestore.core.api.ApiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * The main class of the {CodeStore} Core.
 */
@SpringBootApplication
@Import({StartupListener.class, ApiConfiguration.class})
public class CodeStoreCore {
    public static void main(String[] args) {
        SpringApplication.run(CodeStoreCore.class, args);
    }
}
