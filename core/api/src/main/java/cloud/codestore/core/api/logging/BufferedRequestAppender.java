package cloud.codestore.core.api.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A custom log4j appender for logging requests.
 * This appender logs all statements associated with a single request at once.
 * That way, concurrent requests don't mess up the log file.
 * Additionally, this appender dynamically filters debug statements, only printing them in case of an error.
 */
@Plugin(name = "BufferedRequestAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class BufferedRequestAppender extends AbstractAppender {
    private static BufferedRequestAppender instance;

    private final ConcurrentMap<String, List<LogEvent>> requestLogs = new ConcurrentHashMap<>();
    private final AppenderRef[] appenderRefs;
    private Appender[] delegates;

    private BufferedRequestAppender(String name, AppenderRef[] appenderRefs) {
        super(name, null, null, true, Property.EMPTY_ARRAY);

        this.appenderRefs = appenderRefs;
        instance = this;
        if (appenderRefs == null || appenderRefs.length == 0) {
            throw new IllegalArgumentException("BufferedRequestAppender requires at least one AppenderRef");
        }
    }

    @PluginFactory
    public static BufferedRequestAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("AppenderRef") AppenderRef[] appenderRefs
    ) {
        return new BufferedRequestAppender(name, appenderRefs);
    }

    @Nonnull
    static BufferedRequestAppender getInstance() {
        return instance;
    }

    void flushSuccessfulRequest(String requestId) {
        List<LogEvent> events = requestLogs.remove(requestId);
        if (events != null) {
            for (LogEvent event : events) {
                if (event.getLevel().isMoreSpecificThan(Level.INFO)) {
                    instance.delegate(event);
                }
            }
        }
    }

    void flushFailedRequest(String requestId) {
        List<LogEvent> events = requestLogs.remove(requestId);
        if (events != null) {
            for (LogEvent event : events) {
                instance.delegate(event);
            }
        }
    }

    @Override
    public void start() {
        super.start();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        delegates = Arrays.stream(appenderRefs)
                          .map(appenderRef -> (Appender) config.getAppender(appenderRef.getRef()))
                          .toArray(Appender[]::new);
    }

    @Override
    public void append(LogEvent event) {
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            delegate(event);
        } else {
            requestLogs.computeIfAbsent(requestId, id -> Collections.synchronizedList(new ArrayList<>()))
                       .add(event.toImmutable());
        }
    }

    private void delegate(LogEvent event) {
        for (Appender appender : delegates) {
            appender.append(event);
        }
    }
}
