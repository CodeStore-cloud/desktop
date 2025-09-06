package cloud.codestore.core.application;

import cloud.codestore.core.Snippet;
import cloud.codestore.core.UseCase;
import cloud.codestore.core.Validator;
import cloud.codestore.core.repositories.Repository;
import cloud.codestore.synchronization.ItemSet;
import cloud.codestore.synchronization.MutableItemSynchronization;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.Synchronization;
import org.springframework.beans.factory.annotation.Qualifier;
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
                classes = {UseCase.class, Validator.class, Repository.class}
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

    @Bean
    public Synchronization<Snippet> synchronization(
            @Qualifier("local") ItemSet<Snippet> localSnippetSet,
            @Qualifier("remote") ItemSet<Snippet> remoteSnippetSet,
            Status status
    ) {
        return new MutableItemSynchronization<>(localSnippetSet, remoteSnippetSet, status);
    }
}
