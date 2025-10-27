package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

/**
 * Contains information about the configured cloud service and corresponding credentials.
 */
public record SynchronizationConfiguration(CloudService cloudService) {
    public static SynchronizationConfiguration empty() {
        return new SynchronizationConfiguration();
    }

    public SynchronizationConfiguration(@Nonnull CloudService cloudService) {
        this.cloudService = cloudService;
    }

    private SynchronizationConfiguration() {
        this(CloudService.NONE);
    }

    boolean isCloudServiceConfigured() {
        return cloudService != CloudService.NONE;
    }

    @Override
    @Nonnull
    public CloudService cloudService() {
        return cloudService;
    }
}
