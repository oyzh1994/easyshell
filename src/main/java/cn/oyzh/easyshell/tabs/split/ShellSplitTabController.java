package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * 终端分屏-tab内容组件
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellSplitTabController extends ParentTabController {

    /**
     * 终端1
     */
    @FXML
    private ShellSplitTermController term1Controller;

    /**
     * 终端2
     */
    @FXML
    private ShellSplitTermController term2Controller;

    /**
     * 终端3
     */
    @FXML
    private ShellSplitTermController term3Controller;

    /**
     * 终端4
     */
    @FXML
    private ShellSplitTermController term4Controller;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        // 收起左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout1();
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        // 展开左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout2();
        }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        List<RichTabController> controllers = new ArrayList<>();
        controllers.add(this.term1Controller);
        controllers.add(this.term2Controller);
        if (this.term3Controller != null) {
            controllers.add(this.term3Controller);
        }
        if (this.term4Controller != null) {
            controllers.add(this.term4Controller);
        }
        return controllers;
    }
}
