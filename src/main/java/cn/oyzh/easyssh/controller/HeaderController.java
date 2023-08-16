package cn.oyzh.easyssh.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.node.NodeGroupManage;
import cn.oyzh.easyfx.svg.SVGLabel;
import cn.oyzh.easyfx.view.FXView;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyssh.ssh.SSHEvents;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 主页头部业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
@Lazy
@Slf4j
@Component
public class HeaderController extends FXController {

    /**
     * 项目信息
     */
    @Autowired
    private Project project;

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
    private final NodeGroupManage treeMutexes = new NodeGroupManage();

    /**
     * 设置
     */
    @FXML
    private void setting() {
        FXView fxView = FXViewUtil.getView(SettingController.class);
        if (fxView != null) {
            fxView.toFront();
        } else {
            FXViewUtil.showView(SettingController.class, this.view);
        }
    }

    /**
     * 关于
     */
    @FXML
    private void about() {
        FXViewUtil.showView(AboutController.class, this.view);
    }

    /**
     * 退出
     */
    @FXML
    private void quit() {
        if (FXAlertUtil.confirm("确定退出" + this.project.getName() + "？")) {
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
    public void onViewShown(WindowEvent event) {
        super.onViewShown(event);
        this.treeMutexes.addNodes(this.collapseTree, this.expandTree);
        this.treeMutexes.manageBindVisible();
    }
}
