package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

public interface ReadSynchronizationConfigurationQuery {
    @Nonnull
    SynchronizationConfiguration read();
}
