package cloud.codestore.core.usecases.synchronizesnippets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("The synchronization state")
class SynchronizationStateTest {
    private final SynchronizationState state = new SynchronizationState();

    @Nested
    @DisplayName("after creation")
    class AfterCreation {
        @Test
        @DisplayName("is pending")
        void initialState() {
            var result = new SynchronizationState();
            assertThat(result.getStatus()).isEqualTo(SynchronizationStatus.PENDING);
        }

        @Test
        @DisplayName("has no time set")
        void noTimeSet() {
            var result = new SynchronizationState();
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
            state.start();
        }

        @Test
        @DisplayName("sets the progress to IN_PROGRESS")
        void inProgress() {
            assertThat(state.getStatus()).isEqualTo(SynchronizationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("sets start time")
        void setStartTime() {
            assertThat(state.getStartTime()).isNotNull();
            assertThat(state.getEndTime()).isNull();
            assertThat(state.getDuration()).isZero();
        }

        @Test
        @DisplayName("cannot be started twice")
        void cannotStartTwice() {
            assertThatThrownBy(state::start).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("when completed")
    class Completed {
        @BeforeEach
        void setUp() throws InterruptedException {
            state.start();
            Thread.sleep(500);
            state.complete();
        }

        @Test
        @DisplayName("sets the progress to COMPLETED")
        void inProgress() {
            assertThat(state.getStatus()).isEqualTo(SynchronizationStatus.COMPLETED);
            assertThat(state.getError()).isNull();
        }

        @Test
        @DisplayName("sets the end time")
        void setStartTime() {
            assertThat(state.getStartTime()).isNotNull();
            assertThat(state.getEndTime()).isNotNull();
            assertThat(state.getDuration()).isGreaterThan(500);
        }

        @Test
        @DisplayName("cannot be restarted or finished in any way")
        void dontAllowFurtherActions() {
            assertThatThrownBy(state::start).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(state::complete).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> state.fail(new RuntimeException())).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("when failed")
    class Failed {
        private final Exception exception = new IOException("test");

        @BeforeEach
        void setUp() throws InterruptedException {
            state.start();
            Thread.sleep(500);
            state.fail(exception);
        }

        @Test
        @DisplayName("sets the progress to FAILED")
        void inProgress() {
            assertThat(state.getStatus()).isEqualTo(SynchronizationStatus.FAILED);
        }

        @Test
        @DisplayName("sets the end time")
        void setStartTime() {
            assertThat(state.getStartTime()).isNotNull();
            assertThat(state.getEndTime()).isNotNull();
            assertThat(state.getDuration()).isGreaterThan(500);
        }

        @Test
        @DisplayName("provides access to the error")
        void getError() {
            assertThat(state.getError()).isSameAs(exception);
        }

        @Test
        @DisplayName("cannot be restarted or finished in any way")
        void dontAllowFurtherActions() {
            assertThatThrownBy(state::start).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(state::complete).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> state.fail(new RuntimeException())).isInstanceOf(IllegalStateException.class);
        }
    }
}
