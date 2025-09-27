package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("The synchronization progress")
class SynchronizationProgressTest {
    private SynchronizationProgress progress = new SynchronizationProgress();

    @Nested
    @DisplayName("after creation")
    class AfterCreationTest {
        @Test
        @DisplayName("has status PENDING")
        void statusPending() {
            assertThat(progress.getStatus()).isEqualTo(SynchronizationStatus.PENDING);
        }

        @Test
        @DisplayName("has progress 0")
        void noProgress() {
            assertThat(progress.getProgressInPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("has no start and end time")
        void noTime() {
            assertThat(progress.getStartTime()).isNull();
            assertThat(progress.getEndTime()).isNull();
            assertThat(progress.getDuration()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("after starting the synchronization")
    class AfterStart {
        private OffsetDateTime expectedStartTime;

        @BeforeEach
        void setUp() {
            expectedStartTime = OffsetDateTime.now()
                                              .withOffsetSameInstant(ZoneOffset.UTC)
                                              .truncatedTo(ChronoUnit.SECONDS);

            progress.setStatus(SynchronizationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("has status IN_PROGRESS")
        void statusPending() {
            assertThat(progress.getStatus()).isEqualTo(SynchronizationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("provides the synchronization progress in percent")
        void noProgress() {
            assertThat(progress.getProgressInPercent()).isEqualTo(0);
            progress.setTotalSnippets(4);
            progress.setProcessedSnippets(1);
            assertThat(progress.getProgressInPercent()).isEqualTo(25);
            progress.setProcessedSnippets(4);
            assertThat(progress.getProgressInPercent()).isEqualTo(100);
        }

        @Test
        @DisplayName("has the start time set to the current time")
        void noTime() {
            OffsetDateTime startTime = progress.getStartTime();
            assertThat(startTime).isNotNull();
            assertThat(startTime.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(expectedStartTime);
            assertThat(progress.getEndTime()).isNull();
            assertThat(progress.getDuration()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("after completion")
    class AfterCompletion {
        private static final int MIN_DURATION = 500;
        private OffsetDateTime expectedEndTime;

        @BeforeEach
        void setUp() throws InterruptedException {
            progress.setStatus(SynchronizationStatus.IN_PROGRESS);
            Thread.sleep(MIN_DURATION);
            expectedEndTime = OffsetDateTime.now()
                                            .withOffsetSameInstant(ZoneOffset.UTC)
                                            .truncatedTo(ChronoUnit.SECONDS);

            progress.setStatus(SynchronizationStatus.COMPLETED);
        }

        @Test
        @DisplayName("has status COMPLETED")
        void statusPending() {
            assertThat(progress.getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
        }

        @Test
        @DisplayName("has the end time set to the current time")
        void noTime() {
            OffsetDateTime endTime = progress.getEndTime();
            assertThat(endTime).isNotNull();
            assertThat(endTime.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(expectedEndTime);
            assertThat(progress.getDuration()).isGreaterThan(MIN_DURATION);
        }

        @Test
        @DisplayName("rejects a status change")
        void avoidRestart() {
            assertThatThrownBy(() -> progress.setStatus(SynchronizationStatus.IN_PROGRESS))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}