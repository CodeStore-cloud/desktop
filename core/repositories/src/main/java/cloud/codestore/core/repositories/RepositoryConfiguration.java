package cloud.codestore.core.repositories;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class RepositoryConfiguration {
    private final Directory dataDirectory;

    public RepositoryConfiguration() {
        String userHome = System.getProperty("user.home");
        Path dataPath = Paths.get(userHome, "CodeStore").toAbsolutePath();
        dataDirectory = new Directory(dataPath);
    }

    @Bean("snippetMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                                 .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                 .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                 .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    }

    @Bean("snippets")
    public Directory snippetsDirectory() {
        return dataDirectory.getSubDirectory("snippets");
    }
}
