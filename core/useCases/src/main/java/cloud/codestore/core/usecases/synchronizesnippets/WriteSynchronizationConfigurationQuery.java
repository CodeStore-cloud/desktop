package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

public interface WriteSynchronizationConfigurationQuery {
    void write(@Nonnull SynchronizationConfiguration synchronizationConfiguration);
}
