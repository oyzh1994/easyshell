package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.event.connect.ShellConnectAddedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectImportedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectUpdatedEvent;
import cn.oyzh.easyshell.event.group.ShellAddGroupEvent;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

/**
 * shell连接树
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class ShellConnectTreeView extends RichTreeView implements FXEventListener {

    @Override
    protected void initTreeView() {
        this.dragContent = "shell_connect_tree_drag";
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new ShellRootTreeItem(this));
        this.root().expend();
        super.initRoot();
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 暂停按键处理
        KeyListener.listenReleased(this, KeyCode.PAUSE, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof ShellConnectTreeItem treeItem) {
                treeItem.closeConnect();
            }
        });
    }

    @Override
    public ShellRootTreeItem root() {
        return (ShellRootTreeItem) super.root();
    }

    /**
     * 关闭连接
     */
    public void closeConnects() {
        for (ShellConnectTreeItem treeItem : this.root().getConnectedItems()) {
            ThreadUtil.startVirtual(() -> treeItem.closeConnect(false));
        }
    }

    @Override
    public void expand() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof ShellConnectTreeItem treeItem) {
            treeItem.expend();
        } else if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.expend();
        }
        if (item != null) {
            this.select(item);
        }
    }

    @Override
    public void collapse() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof ShellConnectTreeItem treeItem) {
            treeItem.collapse();
        } else if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.collapse();
        }
        if (item != null) {
            this.select(item);
        }
    }

    /**
     * 添加分组
     *
     * @param event 事件
     */
    @EventSubscribe
    public void addGroup(ShellAddGroupEvent event) {
        this.root().addGroup();
    }

    /**
     * 连接新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectAdded(ShellConnectAddedEvent event) {
        this.root().connectAdded(event.data());
    }

    /**
     * 连接变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectUpdated(ShellConnectUpdatedEvent event) {
        this.root().connectUpdated(event.data());
    }

    /**
     * 连接已导入事件
     */
    @EventSubscribe
    private void connectImported(ShellConnectImportedEvent event) {
        this.root().reloadChild();
    }
}
