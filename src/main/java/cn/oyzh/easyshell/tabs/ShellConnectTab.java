package cn.oyzh.easyshell.tabs;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * shell连接tab
 *
 * @author oyzh
 * @since 2025/05/17
 */
public abstract class ShellConnectTab extends RichTab {

    /**
     * 状态监听
     */
    private ChangeListener<ShellConnState> stateListener=(observable, oldValue, newValue) -> {
        this.flushGraphicColor();
    };

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        // 刷新图标
        this.flush();
//        // 监听连接
        this.client().addStateListener(this.stateListener);
    }

    // public ShellConnectTab() {
    //     super();
    //     // 绑定快捷键
    //     this.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    //         if (OSUtil.isMacOS()) {
    //             if (event.isMetaDown()) {
    //                 if (event.getCode() == KeyCode.W) {
    //                     this.closeTab();
    //                 } else if (event.getCode().isDigitKey()) {
    //                     this.switchTab(event.getCode());
    //                 }
    //             }
    //         } else if (event.isControlDown()) {
    //             if (event.getCode() == KeyCode.W) {
    //                 this.closeTab();
    //             } else if (event.getCode().isDigitKey()) {
    //                 this.switchTab(event.getCode());
    //             }
    //         }
    //     });
    // }

    // /**
    //  * 切换tab
    //  *
    //  * @param code 按键
    //  */
    // private void switchTab(KeyCode code) {
    //     FXTabPane tabPane = (FXTabPane) this.getTabPane();
    //     if (tabPane == null) {
    //         return;
    //     }
    //     int digit = KeyboardUtil.getDigit(code);
    //     if (digit <= 0) {
    //         return;
    //     }
    //     // 选中tab
    //     tabPane.select(digit - 1);
    // }

    /**
     * 获取shell客户端
     *
     * @return shell客户端
     */
    public abstract ShellBaseClient client();

    /**
     * 获取连接
     *
     * @return 连接
     */
    public ShellConnect shellConnect() {
        if (this.client() == null) {
            return null;
        }
        return this.client().getShellConnect();
    }

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

    @Override
    public void flushGraphicColor() {
        if (this.client() == null) {
            return;
        }
        if (!(this.getGraphic() instanceof SVGGlyph)) {
            return;
        }
        SVGGlyph glyph = (SVGGlyph) this.getGraphic();
        if (this.client().isConnected()) {
            glyph.setColor(Color.GREEN);
        } else if (this.client().isClosed()) {
            glyph.setColor(Color.RED);
        } else if (this.client().isConnecting()) {
            glyph.setColor(Color.ORANGE);
        }
    }

    @Override
    public void destroy() {
        this.client().removeStateListener(this.stateListener);
        super.destroy();
    }
}
