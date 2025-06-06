package cn.oyzh.easyshell.tabs;

import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * shell连接tab
 *
 * @author oyzh
 * @since 2025/05/17
 */
public class ShellConnectTab extends RichTab {

    public ShellConnectTab( ) {
        super();
        // 绑定快捷键
        this.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.W && (event.isControlDown() || event.isMetaDown())) {
                this.closeTab();
            }
        });
    }
}
