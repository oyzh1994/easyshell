package cn.oyzh.easyshell.tabs.split;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellParentTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 终端分屏-tab内容组件
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellSplitTabController extends ShellParentTabController {

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
     * 终端5
     */
    @FXML
    private ShellSplitTermController term5Controller;

    /**
     * 终端6
     */
    @FXML
    private ShellSplitTermController term6Controller;

    /**
     * 终端7
     */
    @FXML
    private ShellSplitTermController term7Controller;

    /**
     * 终端8
     */
    @FXML
    private ShellSplitTermController term8Controller;

    /**
     * 终端9
     */
    @FXML
    private ShellSplitTermController term9Controller;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 初始化
     *
     * @param connects 连接列表
     */
    public void init(List<ShellConnect> connects) {
        if (CollectionUtil.isNotEmpty(connects)) {
            List<ShellSplitTermController> controllers = this.getSubControllers();
            List<Runnable> tasks = new ArrayList<>();
            // 从controller批量处理
            for (int i = 0; i < controllers.size(); i++) {
                ShellSplitTermController controller = controllers.get(i);
                ShellConnect connect = CollectionUtil.get(connects, i);
                if (connect == null) {
                    break;
                }
                tasks.add(() -> controller.doConnect(connect));
            }
            // 异步批量初始化
            StageManager.showMask(StageManager.getPrimaryStage(), () -> ThreadUtil.submit(tasks));
        }
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        // 收起左侧
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout1();
        // }
        this.hideLeft();
    }

    // @Override
    // public void onTabClosed(Event event) {
    //     super.onTabClosed(event);
    //     // 展开左侧
    //     if (this.setting.isHiddenLeftAfterConnected()) {
    //         ShellEventUtil.layout2();
    //     }
    // }

    @Override
    public List<ShellSplitTermController> getSubControllers() {
        List<ShellSplitTermController> controllers = new ArrayList<>();
        controllers.add(this.term1Controller);
        controllers.add(this.term2Controller);
        if (this.term3Controller != null) {
            controllers.add(this.term3Controller);
        }
        if (this.term4Controller != null) {
            controllers.add(this.term4Controller);
        }
        if (this.term5Controller != null) {
            controllers.add(this.term5Controller);
        }
        if (this.term6Controller != null) {
            controllers.add(this.term6Controller);
        }
        if (this.term7Controller != null) {
            controllers.add(this.term7Controller);
        }
        if (this.term8Controller != null) {
            controllers.add(this.term8Controller);
        }
        if (this.term9Controller != null) {
            controllers.add(this.term9Controller);
        }
        return controllers;
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws IOException {
        for (ShellSplitTermController controller : this.getSubControllers()) {
            controller.runSnippet(content);
        }
    }
}
