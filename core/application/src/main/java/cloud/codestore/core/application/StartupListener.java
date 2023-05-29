package cloud.codestore.core.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class StartupListener implements ApplicationListener<ServletWebServerInitializedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupListener.class);

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        logServerUrl(port);
    }

    private void logServerUrl(int port) {
        LOGGER.info("Listening on http://localhost:" + port);
    }
}
