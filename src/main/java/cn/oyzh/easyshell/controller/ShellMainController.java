package cn.oyzh.easyshell.controller;

import cn.oyzh.easyshell.controller.llm.ChatController;
import cn.oyzh.easyshell.controller.main.ConnectController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.llm.ShellChatEvent;
import cn.oyzh.easyshell.event.tree.ShellTreeItemChangedEvent;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.controller.ParentStageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.fxml.FXML;

import java.util.List;


/**
 * shell主页
 *
 * @author oyzh
 * @since 2023-08-16
 */
public class ShellMainController extends ParentStageController {

    /**
     * 默认聊天分栏值
     */
    private static final double DEFAULT_CHAT_DIVIDER_POSITION = 0.6;

    /**
     * 跟节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 连接组件
     */
    @FXML
    private FXVBox connect;

    /**
     * 聊天组件
     */
    @FXML
    private FXVBox chat;

    /**
     * shell切换面板
     */
    @FXML
    private ShellTabPane tabPane;

    /**
     * 连接
     */
    @FXML
    private ConnectController connectController;

    /**
     * 聊天
     */
    @FXML
    private ChatController chatController;

    public ShellTabPane getTabPane() {
        return tabPane;
    }

    /**
     * 刷新窗口标题
     *
     * @param connect ssh连接
     */
    private void flushViewTitle(ShellConnect connect) {
        if (connect != null) {
            this.stage.appendTitle(" (" + connect.getName() + ")");
        } else {
            this.stage.restoreTitle();
        }
    }

    /**
     * 树节点变化事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void treeItemChanged(ShellTreeItemChangedEvent event) {
        if (event.data() instanceof ShellConnectTreeItem item) {
            this.flushViewTitle(item.value());
        } else {
            this.flushViewTitle(null);
        }
    }

    /**
     * 布局2
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout2(Layout2Event event) {
        this.connect.display();
        if (!this.root.isShowDivider0()) {
            this.root.setShowDivider0(true);
            double positions_0 = this.root.getPosition0(0.25);
            double positions_1 = this.chat.isVisible() ? this.root.getPosition1(DEFAULT_CHAT_DIVIDER_POSITION) : 1;
            this.root.setDividerPositions(positions_0, positions_1);
        }
    }

    /**
     * 布局1
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout1(Layout1Event event) {
        this.connect.disappear();
        if (this.root.isShowDivider0()) {
            this.root.recordPosition0();
            this.root.setShowDivider0(false);
            double positions_1 = this.chat.isVisible() ? this.root.getPosition1(DEFAULT_CHAT_DIVIDER_POSITION) : 1;
            this.root.setDividerPositions(0, positions_1);
        }
    }

    /**
     * 显示聊天
     */
    private void showChat() {
        double positions_0 = this.connect.isVisible() ? this.root.getPosition0(0.25) : 0;
        double positions_1 = this.root.getPosition1(DEFAULT_CHAT_DIVIDER_POSITION);
        this.chat.display();
        this.root.setShowDivider1(true);
        this.root.setDividerPositions(positions_0, positions_1);
        this.root.requestLayout();
        this.root.layout();
    }

    /**
     * 隐藏聊天
     */
    private void hideChat() {
        if (this.chat.isVisible()) {
            this.root.recordPosition1();
        }
        this.chat.disappear();
        this.root.setShowDivider1(false);
        double positions_0 = this.connect.isVisible() ? this.root.getPosition0(0.25) : 0;
        this.root.setDividerPositions(positions_0, 1);
        this.root.requestLayout();
        this.root.layout();
    }

    /**
     * 聊天
     *
     * @param event 事件
     */
    @EventSubscribe
    public void chat(ShellChatEvent event) {
        if (this.chat.isVisible()) {
            this.hideChat();
        } else {
            this.showChat();
        }
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.hideChat();
    }

    @Override
    public List<SubStageController> getSubControllers() {
        return List.of(this.connectController, this.chatController);
    }
}
