package cloud.codestore.core.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * A custom system tray menu which provides several actions like
 * <ul>
 *     <li>updating the application</li>
 *     <li>shutting down the {CodeStore} Core</li>
 * </ul>
 */
@Component
public class CodeStoreSystemTray {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStoreSystemTray.class);
    private final ResourceBundle resourceBundle;

    private final SystemTray systemTray;
    private final TrayIcon trayIcon;

    @Autowired
    CodeStoreSystemTray(@Nullable SystemTray systemTray) {
        this.resourceBundle = ResourceBundle.getBundle("tray-messages");
        this.systemTray = systemTray;
        if (this.systemTray == null) {
            LOGGER.warn("System Tray is not supported");
            trayIcon = null;
        } else {
            PopupMenu menu = createPopupMenu();
            trayIcon = createTrayIcon(menu);

            try {
                this.systemTray.add(trayIcon);
            } catch (AWTException exception) {
                LOGGER.error("Failed to add tray icon", exception);
            }
        }
    }

    public void showUpdateMessage() {
        if (trayIcon != null) {
            String title = resourceBundle.getString("tray.message.updateAvailable.title");
            String message = resourceBundle.getString("tray.message.updateAvailable.message");
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    public void setUpdateHandler(ActionListener listener) {
        PopupMenu menu = trayIcon.getPopupMenu();
        for (int i = 0; i < menu.getItemCount(); i++) {
            MenuItem item = menu.getItem(i);
            if ("update".equals(item.getName())) {
                item.addActionListener(listener);
                item.setEnabled(true);
            }
        }
    }

    private PopupMenu createPopupMenu() {
        MenuItem exit = new MenuItem(resourceBundle.getString("tray.menu.exit"));
        exit.setName("exit");
        exit.addActionListener(this::exit);

        MenuItem update = new MenuItem(resourceBundle.getString("tray.menu.update"));
        update.setName("update");
        update.setEnabled(false);

        PopupMenu menu = new PopupMenu();
        menu.add(update);
        menu.add(exit);

        return menu;
    }

    private TrayIcon createTrayIcon(PopupMenu menu) {
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/trayicon.png"));
        TrayIcon trayIcon = new TrayIcon(icon, "{CodeStore}", menu);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    private void exit(ActionEvent e) {
        TrayIcon[] trayIcons = systemTray.getTrayIcons();
        if (trayIcons.length > 0) {
            systemTray.remove(trayIcons[0]);
        }

        System.exit(0);
    }
}
