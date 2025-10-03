package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The synchronization result")
class SynchronizationResultTest {
    private final SynchronizationResult result = new SynchronizationResult();

    @Nested
    @DisplayName("after creation")
    class AfterCreation {
        @Test
        @DisplayName("is pending")
        void initialState() {
            var result = new SynchronizationResult();
            assertThat(result.getStatus()).isEqualTo(SynchronizationStatus.PENDING);
        }

        @Test
        @DisplayName("has no time set")
        void noTimeSet() {
            var result = new SynchronizationResult();
            assertThat(result.getStartTime()).isNull();
            assertThat(result.getEndTime()).isNull();
            assertThat(result.getDuration()).isZero();
        }
    }

    @Nested
    @DisplayName("when started")
    class WhenStarted {
        @BeforeEach
        void setUp() {
            result.start();
        }

        @Test
        @DisplayName("sets the progress to IN_PROGRESS")
        void inProgress() {
            assertThat(result.getStatus()).isEqualTo(SynchronizationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("sets start time")
        void setStartTime() {
            assertThat(result.getStartTime()).isNotNull();
            assertThat(result.getEndTime()).isNull();
            assertThat(result.getDuration()).isZero();
        }

        @Test
        @DisplayName("cannot be started twice")
        void cannotStartTwice() {
            assertThatThrownBy(result::start).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("when completed")
    class Completed {
        @BeforeEach
        void setUp() throws InterruptedException {
            result.start();
            Thread.sleep(500);
            result.complete();
        }

        @Test
        @DisplayName("sets the progress to COMPLETED")
        void inProgress() {
            assertThat(result.getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
            assertThat(result.getError()).isNull();
        }

        @Test
        @DisplayName("sets the end time")
        void setStartTime() {
            assertThat(result.getStartTime()).isNotNull();
            assertThat(result.getEndTime()).isNotNull();
            assertThat(result.getDuration()).isGreaterThan(500);
        }

        @Test
        @DisplayName("cannot be restarted or finished in any way")
        void dontAllowFurtherActions() {
            assertThatThrownBy(result::start).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(result::complete).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> result.fail(new RuntimeException())).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("when failed")
    class Failed {
        private final Exception exception = new IOException("test");

        @BeforeEach
        void setUp() throws InterruptedException {
            result.start();
            Thread.sleep(500);
            result.fail(exception);
        }

        @Test
        @DisplayName("sets the progress to FAILED")
        void inProgress() {
            assertThat(result.getStatus()).isEqualTo(SynchronizationStatus.FAILED);
        }

        @Test
        @DisplayName("sets the end time")
        void setStartTime() {
            assertThat(result.getStartTime()).isNotNull();
            assertThat(result.getEndTime()).isNotNull();
            assertThat(result.getDuration()).isGreaterThan(500);
        }

        @Test
        @DisplayName("provides access to the error")
        void getError() {
            assertThat(result.getError()).isSameAs(exception);
        }

        @Test
        @DisplayName("cannot be restarted or finished in any way")
        void dontAllowFurtherActions() {
            assertThatThrownBy(result::start).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(result::complete).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> result.fail(new RuntimeException())).isInstanceOf(IllegalStateException.class);
        }
    }
}
