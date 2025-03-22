package cn.oyzh.easyshell.controller;

import cn.oyzh.easyshell.controller.main.ConnectController;
import cn.oyzh.easyshell.controller.main.MessageController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.tree.ShellTreeItemChangedEvent;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.controller.ParentStageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.stage.WindowEvent;

import java.util.List;


/**
 * shell主页
 *
 * @author oyzh
 * @since 2025/03/06
 */
public class ShellMainController extends ParentStageController {

    /**
     * 配置对象
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 左侧组件
     */
    @FXML
    private FXTabPane tabPaneLeft;

    /**
     * shell切换面板
     */
    @FXML
    private ShellTabPane tabPane;

    /**
     * ssh连接
     */
    @FXML
    private ConnectController connectController;

    /**
     * ssh消息
     */
    @FXML
    private MessageController messageController;

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

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 设置上次保存的页面拉伸
        if (this.setting.isRememberPageResize()) {
            this.resizeLeft(this.setting.getPageLeftWidth());
        }
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        // 保存页面拉伸
        this.savePageResize();
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeLeft(Float newWidth) {
        if (newWidth != null && !Double.isNaN(newWidth)) {
            // 设置组件宽
            this.tabPaneLeft.setRealWidth(newWidth);
            this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            this.tabPaneLeft.parentAutosize();
        }
    }

    @Override
    public void onSystemExit() {
        // 保存页面拉伸
        this.savePageResize();
    }

    /**
     * 保存页面拉伸
     */
    private void savePageResize() {
        if (this.setting.isRememberPageResize()) {
            this.setting.setPageLeftWidth((float) this.tabPaneLeft.getMinWidth());
            ShellSettingStore.INSTANCE.replace(this.setting);
        }
    }

    @Override
    protected void bindListeners() {
        // 大小调整增强
        NodeWidthResizer resizer = new NodeWidthResizer(this.tabPaneLeft, Cursor.DEFAULT, this::resizeLeft);
        resizer.widthLimit(240f, 650f);
        resizer.initResizeEvent();
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
     */
    @EventSubscribe
    private void layout2(Layout2Event event) {
        this.tabPaneLeft.display();
        double w = this.tabPaneLeft.realWidth();
        this.tabPane.setLayoutX(w);
        this.tabPane.setFlexWidth("100% - " + w);
        this.tabPaneLeft.parentAutosize();
    }

    /**
     * 布局1
     */
    @EventSubscribe
    private void layout1(Layout1Event event) {
        this.tabPaneLeft.disappear();
        this.tabPane.setLayoutX(0);
        this.tabPane.setFlexWidth("100%");
        this.tabPaneLeft.parentAutosize();
    }

    @Override
    public List<SubStageController> getSubControllers() {
        return List.of(this.connectController, this.messageController);
    }
}
