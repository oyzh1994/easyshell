package cn.oyzh.easyshell.tabs;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * shell连接tab
 *
 * @author oyzh
 * @since 2025/05/17
 */
public abstract class ShellConnectTab extends RichTab {

    public ShellConnectTab() {
        super();
        // 绑定快捷键
        this.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.W && (event.isControlDown() || event.isMetaDown())) {
                this.closeTab();
            }
        });
    }

    /**
     * 获取连接
     *
     * @return 连接
     */
    public abstract ShellConnect shellConnect();

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> menuItems = super.getMenuItems();
        ShellConnect connect = this.shellConnect();
        if (connect != null) {
            MenuItem cloneSession = MenuItemHelper.cloneSession(this::cloneSession);
            menuItems.add(cloneSession);
        }
        return menuItems;
    }

    /**
     * 克隆会话
     */
    private void cloneSession() {
        ShellEventUtil.connectionOpened(this.shellConnect());
    }
}
