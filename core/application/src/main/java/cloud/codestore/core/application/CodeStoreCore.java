package cloud.codestore.core.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class of the {CodeStore} Core.
 */
@SpringBootApplication(scanBasePackages = "cloud.codestore.core")
public class CodeStoreCore {
    public static void main(String[] args) {
        SpringApplication.run(CodeStoreCore.class, args);
    }
}
