package cloud.codestore.core.application;

import cloud.codestore.core.Sync;
import cloud.codestore.core.UseCase;
import cloud.codestore.core.Validator;
import cloud.codestore.core.repositories.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.awt.*;

/**
 * The main class of the {CodeStore} Core.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "cloud.codestore.core",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = {UseCase.class, Validator.class, Sync.class, Repository.class}
        )
)
public class CodeStoreCore {
    public static void main(String[] args) {
        SpringApplication.run(CodeStoreCore.class, args);
    }

    @Bean
    public SystemTray systemTray() {
        System.setProperty("java.awt.headless", "false");
        return SystemTray.isSupported() ? SystemTray.getSystemTray() : null;
    }
}
