package cloud.codestore.core.usecases.synchronizesnippets;

/**
 * Contains information about the configured cloud service and corresponding credentials.
 */
public class SynchronizationConfiguration {
    private final CloudService cloudService;

    public static SynchronizationConfiguration empty() {
        return new SynchronizationConfiguration();
    }

    public SynchronizationConfiguration(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    private SynchronizationConfiguration() {
        cloudService = null;
    }

    boolean isCloudServiceConfigured() {
        return cloudService != null;
    }
}
