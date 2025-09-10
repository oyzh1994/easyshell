package cn.oyzh.easyshell.tabs;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
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
            if (OSUtil.isMacOS()) {
                if (event.isMetaDown()) {
                    if (event.getCode() == KeyCode.W) {
                        this.closeTab();
                    } else if (event.getCode().isDigitKey()) {
                        this.switchTab(event.getCode());
                    }
                }
            } else if (event.isControlDown()) {
                if (event.getCode() == KeyCode.W) {
                    this.closeTab();
                } else if (event.getCode().isDigitKey()) {
                    this.switchTab(event.getCode());
                }
            }
        });
    }

    /**
     * 切换tab
     *
     * @param code 按键
     */
    private void switchTab(KeyCode code) {
        FXTabPane tabPane = (FXTabPane) this.getTabPane();
        if (tabPane == null) {
            return;
        }
        int digit = KeyboardUtil.getDigit(code);
        if (digit <= 0) {
            return;
        }
        // 选中tab
        tabPane.select(digit - 1);
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
            MenuItem copySession = MenuItemHelper.copyThisSession(this::copySession);
            menuItems.add(copySession);
        }
        return menuItems;
    }

    /**
     * 复制会话
     */
    private void copySession() {
        ShellEventUtil.connectionOpened(this.shellConnect());
    }
}
