package cloud.codestore.core.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * A custom system tray menu which provides several actions like
 * <ul>
 *     <li>updating the application</li>
 *     <li>shutting down the {CodeStore} Core</li>
 * </ul>
 */
@Component
class CodeStoreSystemTray {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStoreSystemTray.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("tray-messages");

    private final SystemTray tray;

    @Autowired
    CodeStoreSystemTray(@Nullable SystemTray systemTray) {
        tray = systemTray;
        if (tray == null) {
            LOGGER.warn("System Tray is not supported");
        } else {
            PopupMenu menu = createPopupMenu();
            TrayIcon trayIcon = createTrayIcon(menu);

            try {
                tray.add(trayIcon);
            } catch (AWTException exception) {
                LOGGER.error("Failed to add tray icon", exception);
            }
        }
    }

    private PopupMenu createPopupMenu() {
        MenuItem exit = new MenuItem(resourceBundle.getString("tray.menu.exit"));
        exit.addActionListener(this::exit);

        PopupMenu menu = new PopupMenu();
        menu.add(exit);

        return menu;
    }

    private TrayIcon createTrayIcon(PopupMenu menu) {
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/trayicon.png"));
        TrayIcon trayIcon = new TrayIcon(icon, "{CodeStore} Core", menu);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    private void exit(ActionEvent e) {
        TrayIcon[] trayIcons = tray.getTrayIcons();
        if (trayIcons.length > 0) {
            tray.remove(trayIcons[0]);
        }

        System.exit(0);
    }
}
