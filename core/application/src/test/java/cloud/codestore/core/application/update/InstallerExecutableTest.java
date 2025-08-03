package cloud.codestore.core.application.update;

import org.junit.jupiter.api.*;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("The installer executable")
class InstallerExecutableTest {

    private static final byte[] TEST_CONTENT = randomContent();

    private InstallerExecutable installer;

    @BeforeEach
    void setUp() {
        InputStream inputStream = new ByteArrayInputStream(TEST_CONTENT);
        installer = new InstallerExecutable(TEST_CONTENT.length, inputStream);
    }

    @Nested
    @DisplayName("when downloading")
    class DownloadingTest {
        @AfterEach
        void tearDown() throws NoSuchFieldException, IOException {
            Files.deleteIfExists(getTempFile(installer));
        }

        @Test
        @DisplayName("writes the content from the input stream to a file")
        void writeInputStreamToFile() throws IOException, NoSuchFieldException {
            installer.download();

            Path tempFile = getTempFile(installer);
            assertThat(tempFile).exists().hasBinaryContent(TEST_CONTENT);
        }

        @Test
        @DisplayName("calls the progress listener while consuming the stream")
        void callProgressListener() throws IOException {
            AtomicInteger progressListenerCount = new AtomicInteger();
            AtomicReference<Double> progress = new AtomicReference<>((double) 0);
            installer.setProgressListener(value -> {
                progressListenerCount.incrementAndGet();
                progress.set(value);
            });

            installer.download();

            assertThat(progressListenerCount.get()).isEqualTo(20);
            assertThat(progress.get()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cancels the download when the cancel flag was set")
        void cancel() throws IOException, NoSuchFieldException {
            AtomicBoolean cancelFlag = new AtomicBoolean(false);
            installer.setProgressListener(progress -> {
                assertThat(cancelFlag.get()).withFailMessage("Already canceled").isFalse();
                if (progress > 0.5) {
                    cancelFlag.set(true);
                    installer.cancelDownload();
                }
            });

            installer.download();

            Path tempFile = getTempFile(installer);
            assertThat(tempFile).doesNotExist();
        }

        private Path getTempFile(InstallerExecutable installer) throws NoSuchFieldException {
            Field field = InstallerExecutable.class.getDeclaredField("file");
            ReflectionUtils.makeAccessible(field);
            return (Path) ReflectionUtils.getField(field, installer);
        }
    }

    private static byte[] randomContent() {
        int tenMB = 1024 * 1024 * 10;
        byte[] data = new byte[tenMB];

        Random random = new Random();
        random.nextBytes(data);
        return data;
    }
}
