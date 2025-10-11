package cloud.codestore.core.application.synchronization;

import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationProcess;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SynchronizationExecutor {
    private final SynchronizationProcess synchronizationProcess;

    SynchronizationExecutor(SynchronizationProcess synchronizationProcess) {
        this.synchronizationProcess = synchronizationProcess;
    }

    @EventListener(ApplicationReadyEvent.class)
    void synchronizeSnippets() {
        if (!synchronizationProcess.isSkipped()) {
            synchronizationProcess.execute();
        }
    }
}
