package cloud.codestore.core.repositories;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class RepositoryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfiguration.class);

    @Value("${codestore.data:}")
    private String dataPath;

    @Bean("snippetMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                                 .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                 .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                 .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    }

    @Bean("data")
    public Directory dataDirectory() {
        Directory dataDirectory;
        if (dataPath.isEmpty()) {
            String userHome = System.getProperty("user.home");
            Path dataPath = Paths.get(userHome, "CodeStore").toAbsolutePath();
            dataDirectory = new Directory(dataPath);
        } else {
            dataDirectory = new Directory(Path.of(dataPath).toAbsolutePath());
        }

        LOGGER.info("Data directory: {}", dataDirectory);
        return dataDirectory;
    }

    @Bean("snippets")
    public Directory snippetsDirectory(@Qualifier("data") Directory dataDirectory) {
        return dataDirectory.getSubDirectory("snippets");
    }
}
