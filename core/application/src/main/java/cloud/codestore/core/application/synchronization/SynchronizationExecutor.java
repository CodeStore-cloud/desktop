package cloud.codestore.core.application.synchronization;

import cloud.codestore.core.usecases.synchronizesnippets.ReadSynchronizationConfigurationQuery;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProcess;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SynchronizationExecutor {
    private final ReadSynchronizationConfigurationQuery readSynchronizationConfigurationQuery;
    private final SynchronizationProcess synchronizationProcess;

    SynchronizationExecutor(
            ReadSynchronizationConfigurationQuery readSynchronizationConfigurationQuery,
            SynchronizationProcess synchronizationProcess
    ) {
        this.readSynchronizationConfigurationQuery = readSynchronizationConfigurationQuery;
        this.synchronizationProcess = synchronizationProcess;
    }

    @EventListener(ApplicationReadyEvent.class)
    void synchronizeSnippets() {
        var configuration = readSynchronizationConfigurationQuery.read();
        synchronizationProcess.execute(configuration);
    }
}
