package cn.oyzh.easyshell.tabs;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import javafx.event.Event;

/**
 * shell父级标签页控制器，提供程序设置及左右侧栏收放能力
 *
 * @author oyzh
 * @since 2025-08-25
 */
public class ShellParentTabController extends ParentTabController {

    /**
     * 设置
     */
    protected ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 收起左侧
     */
    protected void hideLeft() {
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout1();
        }
    }

    /**
     * 显示左侧
     */
    protected void showLeft() {
//        if (this.setting.isHiddenLeftAfterConnected()) {
//            ShellEventUtil.layout2();
//        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.showLeft();
    }
}
