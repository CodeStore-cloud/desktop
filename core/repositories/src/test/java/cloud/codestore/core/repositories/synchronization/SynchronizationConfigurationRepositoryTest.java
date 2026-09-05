package cloud.codestore.core.repositories.synchronization;

import cloud.codestore.core.repositories.File;
import cloud.codestore.core.usecases.synchronizesnippets.CloudService;
import cloud.codestore.core.usecases.synchronizesnippets.SynchronizationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization configuration repository")
class SynchronizationConfigurationRepositoryTest {

    @Mock
    private File file;
    private SynchronizationConfigurationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SynchronizationConfigurationRepository(file);
    }

    @Test
    @DisplayName("returns an empty configuration if the configuration file does not exist")
    void fileNotExists() {
        when(file.exists()).thenReturn(false);

        SynchronizationConfiguration result = repository.read();

        assertThat(result).isEqualTo(SynchronizationConfiguration.empty());
    }

    @Test
    @DisplayName("returns empty configuration when file exists but properties are empty")
    void emptyFile() {
        when(file.exists()).thenReturn(true);
        when(file.readProperties()).thenReturn(new Properties());

        SynchronizationConfiguration result = repository.read();

        assertThat(result).isEqualTo(SynchronizationConfiguration.empty());
    }

    @Test
    @DisplayName("returns the configuration from the file")
    void fileNotEmpty() {
        when(file.exists()).thenReturn(true);
        Properties properties = new Properties();
        properties.setProperty("serviceName", "GOOGLE_DRIVE");
        when(file.readProperties()).thenReturn(properties);

        SynchronizationConfiguration result = repository.read();

        assertThat(result).isEqualTo(new SynchronizationConfiguration(CloudService.GOOGLE_DRIVE));
    }

    @Test
    @DisplayName("writes the configuration to the file")
    void writeConfigurationToFile() {
        SynchronizationConfiguration configuration = new SynchronizationConfiguration(CloudService.GOOGLE_DRIVE);

        repository.write(configuration);

        ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
        verify(file).write(captor.capture());
        String serviceName = captor.getValue().getProperty("serviceName");
        assertThat(serviceName).isEqualTo(CloudService.GOOGLE_DRIVE.name());
    }
}
