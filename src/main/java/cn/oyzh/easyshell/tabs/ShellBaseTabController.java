package cn.oyzh.easyshell.tabs;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;

/**
 * @author oyzh
 * @since 2025-08-25
 */
public class ShellBaseTabController extends RichTabController {

    /**
     * 设置
     */
    protected ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 收起左侧
     */
    protected void hideLeft() {
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout1();
        // }
    }

    /**
     * 显示左侧
     */
    protected void showLeft() {
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout2();
        // }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.showLeft();
    }
}
