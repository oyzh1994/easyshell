package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.event.connect.SSHConnectAddedEvent;
import cn.oyzh.easyssh.event.connect.SSHConnectUpdatedEvent;
import cn.oyzh.easyssh.event.group.SSHAddGroupEvent;
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
 * ssh连接树
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class SSHConnectTreeView extends RichTreeView implements FXEventListener {

    @Override
    protected void initTreeView() {
        this.dragContent = "ssh_connect_tree_drag";
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    protected void initRoot() {
        super.setRoot(new SSHRootTreeItem(this));
        this.root().expend();
        super.initRoot();
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 暂停按键处理
        KeyListener.listenReleased(this, KeyCode.PAUSE, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof SSHConnectTreeItem treeItem) {
                treeItem.closeConnect();
            }
        });
    }

    @Override
    public SSHRootTreeItem root() {
        return (SSHRootTreeItem) super.root();
    }

    /**
     * 关闭连接
     */
    public void closeConnects() {
        for (SSHConnectTreeItem treeItem : this.root().getConnectedItems()) {
            ThreadUtil.startVirtual(() -> treeItem.closeConnect(false));
        }
    }

    @Override
    public void expand() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof SSHConnectTreeItem treeItem) {
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
        if (item instanceof SSHConnectTreeItem treeItem) {
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
    public void addGroup(SSHAddGroupEvent event) {
        this.root().addGroup();
    }

    /**
     * 连接新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectAdded(SSHConnectAddedEvent event) {
        this.root().connectAdded(event.data());
    }

    /**
     * 连接变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectUpdated(SSHConnectUpdatedEvent event) {
        this.root().connectUpdated(event.data());
    }
}
