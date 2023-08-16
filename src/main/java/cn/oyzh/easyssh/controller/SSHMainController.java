package cn.oyzh.easyssh.controller;

import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.controls.FlexVBox;
import cn.oyzh.easyfx.event.EventReceiver;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.keyboard.KeyboardListener;
import cn.oyzh.easyfx.node.ResizeEnhance;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.domain.SSHPageInfo;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.fx.SSHConnectTreeItem;
import cn.oyzh.easyssh.fx.SSHTreeView;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.PageInfoStore;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.easyssh.tabs.SSHTabPane;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * ssh键主页
 *
 * @author oyzh
 * @since 2023/06/22
 */
@Lazy
@Slf4j
@Component
public class SSHMainController extends FXController {

    /**
     * 配置对象
     */
    private final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 当前激活的ssh信息
     */
    private SSHInfo info;

    /**
     * 左侧组件
     */
    @FXML
    private FlexVBox mainLeft;

    /**
     * 左侧ssh树
     */
    @FXML
    private SSHTreeView tree;

    /**
     * 大小调整增强
     */
    private ResizeEnhance resizeEnhance;

    /**
     * ssh切换面板
     */
    @FXML
    private SSHTabPane tabPane;

    /**
     * 页面信息
     */
    private final SSHPageInfo pageInfo = PageInfoStore.PAGE_INFO;

    /**
     * 页面信息储存
     */
    private final PageInfoStore pageInfoStore = PageInfoStore.INSTANCE;

    /**
     * ssh信息修改事件
     *
     * @param info ssh信息
     */
    @EventReceiver(value = SSHEvents.SSH_INFO_UPDATED, async = true)
    private void onInfoUpdate(SSHInfo info) {
        if (this.info == info) {
            this.view.appendTitle(" (" + info.getName() + ")");
        }
    }

    /**
     * 树节点变化事件
     *
     * @param item 节点
     */
    private void treeItemChanged(TreeItem<?> item) {
        if (item instanceof SSHConnectTreeItem treeItem) {
            this.connectTreeItemChanged(treeItem);
        } else {
            this.connectTreeItemChanged(null);
        }
    }

    /**
     * 连接节点变化事件
     *
     * @param item 连接节点
     */
    private void connectTreeItemChanged(SSHConnectTreeItem item) {
        if (item == null) {
            this.info = null;
            this.view.restoreTitle();
        } else if (this.info != item.value()) {
            this.info = item.value();
            this.onInfoUpdate(this.info);
        }
        EventUtil.fire(SSHEvents.CONNECTION_CHANGED, item);
    }

    @Override
    public void onViewShown(WindowEvent event) {
        super.onViewShown(event);
        // 注册事件处理
        EventUtil.register(this);
        EventUtil.register(this.tree);
        EventUtil.register(this.tabPane);

        // 设置上次保存的页面拉伸
        if (this.setting.isRememberPageResize()) {
            this.resizeMainLeft(this.pageInfo.getMainLeftWidth());
        }
    }

    @Override
    public void onViewHidden(WindowEvent event) {
        super.onViewHidden(event);
        // 取消注册事件处理
        EventUtil.unregister(this);
        EventUtil.unregister(this.tree);
        EventUtil.unregister(this.tabPane);
        // 关闭连接
        this.tree.closeConnects();
        // 保存页面拉伸
        this.savePageResize();
        // 取消F5按键监听
        KeyboardListener.unListenKeyReleased(this.tree, KeyCode.F5);
        KeyboardListener.unListenKeyReleased(this.tabPane, KeyCode.F5);
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeMainLeft(Double newWidth) {
        if (newWidth != null && !Double.isNaN(newWidth)) {
            // 设置组件宽
            this.mainLeft.setWidthAll(newWidth);
            this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            this.mainLeft.parentAutosize();
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
            this.pageInfo.setMainLeftWidth(this.mainLeft.getMinWidth());
            this.pageInfoStore.update(this.pageInfo);
        }
    }

    @Override
    protected void bindListeners() {
        this.tabPane.selectedTabChanged((abs, o, n) -> {
            if (o != null) {
                o.getStyleClass().remove("tab-active");
            }
            if (n != null) {
                n.getStyleClass().add("tab-active");
            }
        });
        // ssh树键变化事件
        this.tree.treeItemChanged(this::treeItemChanged);

        // 文件拖拽相关
        this.view.getScene().setOnDragOver(event1 -> {
            // 忽略ssh树的拖动
            Dragboard dragboard = event1.getDragboard();
            if (dragboard != null && Objects.equals(dragboard.getString(), "ssh_tree_drag")) {
                return;
            }
            this.view.disable();
            this.view.appendTitle("===松开鼠标以释放文件===");
            event1.acceptTransferModes(TransferMode.ANY);
            event1.consume();
        });
        this.view.getScene().setOnDragExited(event1 -> {
            // 忽略ssh树的拖动
            Dragboard dragboard = event1.getDragboard();
            if (dragboard != null && Objects.equals(dragboard.getString(), "ssh_tree_drag")) {
                return;
            }
            this.view.enable();
            this.view.restoreTitle();
            event1.consume();
        });
        this.view.getScene().setOnDragDropped(event1 -> {
            // 忽略ssh树的拖动
            Dragboard dragboard = event1.getDragboard();
            if (dragboard != null && Objects.equals(dragboard.getString(), "ssh_tree_drag")) {
                return;
            }
            this.tree.root().dragFile(event1);
            event1.setDropCompleted(true);
            event1.consume();
        });

        // 拖动改变ssh树大小处理
        this.resizeEnhance = new ResizeEnhance(this.mainLeft, Cursor.DEFAULT);
        this.resizeEnhance.minWidth(390d);
        this.resizeEnhance.maxWidth(800d);
        this.resizeEnhance.triggerThreshold(8d);
        this.resizeEnhance.mouseDragged(event -> {
            double sceneX = event.getSceneX();
            if (this.resizeEnhance.resizeWidthAble(sceneX)) {
                // 左侧组件重新布局
                this.resizeMainLeft(sceneX);
            }
        });

        // 初始化拉伸事件
        this.tree.setOnMouseMoved(this.resizeEnhance.mouseMoved());
        this.resizeEnhance.initResizeEvent();

        // 监听F5按键
        KeyboardListener.listenKeyReleased(this.tree, KeyCode.F5, keyEvent -> this.tree.reload());
    }

    /**
     * 展开左侧
     */
    @EventReceiver(value = SSHEvents.LEFT_EXTEND, async = true, verbose = true)
    private void leftExtend() {
        this.mainLeft.showNode();
        double w = this.mainLeft.getMinWidth();
        this.tabPane.setLayoutX(w);
        this.tabPane.setFlexWidth("100% - " + w);
        this.mainLeft.parentAutosize();
        log.info("LEFT_EXTEND.");
    }

    /**
     * 收缩左侧
     */
    @EventReceiver(value = SSHEvents.LEFT_COLLAPSE, async = true, verbose = true)
    private void leftCollapse() {
        this.mainLeft.hideNode();
        this.tabPane.setLayoutX(0);
        this.tabPane.setFlexWidth("100%");
        this.mainLeft.parentAutosize();
        log.info("LEFT_COLLAPSE.");
    }
}
