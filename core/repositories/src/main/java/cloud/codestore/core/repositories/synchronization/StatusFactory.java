package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.Directory;
import cloud.codestore.core.repositories.File;
import cloud.codestore.synchronization.Status;
import cloud.codestore.synchronization.helper.CsvMutableItemStatus;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
class StatusFactory implements FactoryBean<Status> {
    private final File statusFile;

    StatusFactory(@Qualifier("data") Directory dataDirectory) {
        statusFile = dataDirectory.getFile("SyncStatus.csv");
    }

    @Override
    public Status getObject() {
        // TODO log exception in case of error
        return CsvMutableItemStatus.loadSilently(statusFile.path());
    }

    @Override
    public Class<?> getObjectType() {
        return Status.class;
    }
}
