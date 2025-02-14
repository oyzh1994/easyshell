package cn.oyzh.easyssh.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeMutexes;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * 主页头部业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
public class HeaderController extends SubStageController {

    /**
     * 项目信息
     */
    private final Project project = Project.load();

    /**
     * 展开ssh树
     */
    @FXML
    private SVGLabel expandTree;

    /**
     * 收缩ssh树
     */
    @FXML
    private SVGLabel collapseTree;

    /**
     * ssh树互斥器
     */
    private final NodeMutexes treeMutexes = new NodeMutexes();

    /**
     * 设置
     */
    @FXML
    private void setting() {
        StageAdapter fxView = StageManager.getStage(SettingController.class);
        if (fxView != null) {
            fxView.toFront();
        } else {
            StageManager.showStage(SettingController.class, this.stage);
        }
    }

    /**
     * 关于
     */
    @FXML
    private void about() {
        StageManager.showStage(AboutController.class, this.stage);
    }

    /**
     * 退出
     */
    @FXML
    private void quit() {
        if (MessageBox.confirm("确定退出" + this.project.getName() + "？")) {
            EventUtil.fire(SSHEvents.APP_EXIT);
        }
    }

    /**
     * 收缩左侧ssh树
     */
    @FXML
    private void collapseTree() {
        this.treeMutexes.visible(this.expandTree);
        EventUtil.fire(SSHEvents.LEFT_COLLAPSE);
    }

    /**
     * 展开左侧ssh树
     */
    @FXML
    private void expandTree() {
        this.treeMutexes.visible(this.collapseTree);
        EventUtil.fire(SSHEvents.LEFT_EXTEND);
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeMutexes.addNodes(this.collapseTree, this.expandTree);
        this.treeMutexes.manageBindVisible();
    }
}
