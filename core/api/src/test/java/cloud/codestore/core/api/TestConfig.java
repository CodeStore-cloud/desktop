package cloud.codestore.core.api;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({ApiConfiguration.class})
public class TestConfig {}
