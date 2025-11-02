package cloud.codestore.core.usecases.synchronizesnippets;

import javax.annotation.Nonnull;

/**
 * Contains information about the configured cloud service and corresponding credentials.
 */
public record SynchronizationConfiguration(@Nonnull CloudService cloudService) {
    public static SynchronizationConfiguration empty() {
        return new SynchronizationConfiguration(CloudService.NONE);
    }

    boolean isCloudServiceConfigured() {
        return cloudService != CloudService.NONE;
    }
}
