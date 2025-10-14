package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.File;
import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.ReadSynchronizationConfigurationQuery;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Properties;

@Component
class SynchronizationConfigurationRepository implements ReadSynchronizationConfigurationQuery {
    private final File syncConfig;

    SynchronizationConfigurationRepository(@Nonnull @Qualifier("sync") File syncConfig) {
        this.syncConfig = syncConfig;
    }

    @Nonnull
    @Override
    public SynchronizationConfiguration read() {
        if (syncConfig.exists()) {
            Properties properties = syncConfig.readProperties();
            if (!properties.isEmpty()) {
                String serviceName = properties.getProperty("serviceName");
                CloudService service = CloudService.valueOf(serviceName);
                return new SynchronizationConfiguration(service);
            }
        }

        return SynchronizationConfiguration.empty();
    }
}
