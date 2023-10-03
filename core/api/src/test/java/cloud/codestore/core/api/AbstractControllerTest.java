package cloud.codestore.core.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import({TestConfig.class, ErrorHandler.class})
@ExtendWith({DefaultLocale.class, DummyWebServerInitializedEvent.class})
public abstract class AbstractControllerTest {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;
}
