package cloud.codestore.core.application;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("The system tray")
class CodeStoreSystemTrayTest {
    private static final int UPDATE_MENU_ITEM_INDEX = 0;
    private static final int EXIT_MENU_ITEM_INDEX = 1;

    @Mock
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private CodeStoreSystemTray codeStoreTray;

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

        codeStoreTray = new CodeStoreSystemTray(systemTray);
    }

    @Test
    @DisplayName("shows an icon")
    void showsIcon() {
        assertThat(trayIcon.getImage()).isNotNull();
    }

    @Test
    @DisplayName("enables the update menu item when setting the corresponding event listener")
    void enableUpdateButton() {
        MenuItem item = trayIcon.getPopupMenu().getItem(UPDATE_MENU_ITEM_INDEX);
        assertThat(item.isEnabled()).isFalse();

        codeStoreTray.setUpdateHandler(event -> {});

        assertThat(item.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("displays update notification message")
    void showsUpdateMessage() throws NoSuchFieldException {
        TrayIcon spyTrayIcon = spy(trayIcon);
        Field trayIconField = CodeStoreSystemTray.class.getDeclaredField("trayIcon");
        ReflectionUtils.makeAccessible(trayIconField);
        ReflectionUtils.setField(trayIconField, codeStoreTray, spyTrayIcon);

        codeStoreTray.showUpdateMessage();

        verify(spyTrayIcon).displayMessage(
            "Update Available",
            "There is an update available for {CodeStore}!",
            TrayIcon.MessageType.INFO
        );
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
            MenuItem item = menu.getItem(EXIT_MENU_ITEM_INDEX);
            assertThat(item.getName()).isEqualTo("exit");
            assertThat(item.getLabel()).isEqualTo("Exit {CodeStore} Core");
            assertThat(item.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("update")
        void updateItem() {
            MenuItem item = menu.getItem(UPDATE_MENU_ITEM_INDEX);
            assertThat(item.getName()).isEqualTo("update");
            assertThat(item.getLabel()).isEqualTo("Update");
            assertThat(item.isEnabled()).isFalse();
        }
    }
}
