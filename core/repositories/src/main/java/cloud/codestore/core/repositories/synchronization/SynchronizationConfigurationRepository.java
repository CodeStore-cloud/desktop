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
    private static final String SERVICE_NAME = "serviceName";

    private final File syncConfigFile;

    SynchronizationConfigurationRepository(@Nonnull @Qualifier("syncProperties") File syncConfigFile) {
        this.syncConfigFile = syncConfigFile;
    }

    @Nonnull
    @Override
    public SynchronizationConfiguration read() {
        if (syncConfigFile.exists()) {
            Properties properties = syncConfigFile.readProperties();
            if (!properties.isEmpty()) {
                String serviceName = properties.getProperty(SERVICE_NAME);
                CloudService service = CloudService.valueOf(serviceName);
                return new SynchronizationConfiguration(service);
            }
        }

        return SynchronizationConfiguration.empty();
    }

    @Override
    public void write(@Nonnull SynchronizationConfiguration synchronizationConfiguration) {
        Properties properties = new Properties();
        properties.put(SERVICE_NAME, synchronizationConfiguration.cloudService().name());
        syncConfigFile.write(properties);
    }
}
