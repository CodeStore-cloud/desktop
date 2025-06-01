package cloud.codestore.core.application;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
@DisplayName("The system tray")
class CodeStoreSystemTrayTest {
    @Mock
    private SystemTray systemTray;
    private TrayIcon trayIcon;

    @BeforeAll
    static void beforeAll() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() throws AWTException {
        doAnswer(invocation -> {
            trayIcon = invocation.getArgument(0, TrayIcon.class);
            return null;
        }).when(systemTray).add(any(TrayIcon.class));

        new CodeStoreSystemTray(systemTray);
    }

    @Test
    @DisplayName("shows an icon")
    void showsIcon() {
        assertThat(trayIcon.getImage()).isNotNull();
    }

    @Nested
    @DisplayName("provides a popup menu containing")
    class PopupMenuTest {
        private PopupMenu menu;

        @BeforeEach
        void setUp() {
            menu = trayIcon.getPopupMenu();
        }

        @Test
        @DisplayName("exit")
        void exitItem() {
            MenuItem item = menu.getItem(0);
            assertThat(item.getLabel()).isEqualTo("Exit {CodeStore} Core");
        }
    }
}