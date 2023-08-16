package cn.oyzh.easyssh.fx;

import cn.oyzh.common.thread.TimerUtil;
import cn.oyzh.easyfx.controls.FlexTreeView;
import cn.oyzh.easyfx.event.EventReceiver;
import cn.oyzh.easyfx.keyboard.KeyboardListener;
import cn.oyzh.easyfx.util.MouseUtil;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.SSHSettingStore;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;
import javafx.util.Callback;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * ssh树
 *
 * @author oyzh
 * @since 2023/1/29
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class SSHTreeView extends FlexTreeView {

    /**
     * 子节点变化处理
     */
    @Setter
    @Getter
    private Runnable childChanged;

    /**
     * 连接关闭处理
     */
    @Setter
    @Getter
    private Consumer<SSHConnectTreeItem> connectClosed;

    /**
     * 连接完成处理
     */
    @Setter
    @Getter
    private Consumer<SSHConnectTreeItem> connectConnected;

    /**
     * 配置储存对象
     */
    private final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 触发子节点变化事件
     */
    public void fireChildChanged() {
        if (this.childChanged != null) {
            try {
                this.childChanged.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 触发连接关闭事件
     */
    public void fireConnectClosed(@NonNull SSHConnectTreeItem item) {
        if (this.connectClosed != null) {
            try {
                this.connectClosed.accept(item);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 触发连接完成事件
     */
    public void fireConnectConnected(@NonNull SSHConnectTreeItem item) {
        if (this.connectConnected != null) {
            try {
                this.connectConnected.accept(item);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public SSHTreeView() {
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new SSHTreeCell());
        // 初始化事件处理
        this.initEventHandler();
        super.root(new SSHRootTreeItem(this));
        this.root().extend();
    }

    @Override
    public SSHRootTreeItem root() {
        return (SSHRootTreeItem) this.getRoot();
    }

    /**
     * 关闭连接
     */
    public void closeConnects() {
        for (SSHConnectTreeItem treeItem : this.root().getConnectedItems()) {
            TimerUtil.start(treeItem::disConnect);
        }
    }

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    public Window window() {
        return this.getScene().getWindow();
    }

    @Override
    public void selectAndScroll(TreeItem<?> item) {
        if (item != null) {
            super.selectAndScroll(item);
        } else {
            this.clearSelection();
        }
    }

    /**
     * 初始化事件处理器
     */
    protected void initEventHandler() {
        // 主鼠标按钮点击事件
        super.setOnMousePrimaryClicked(e -> {
            TreeItem<?> item = this.getSelectedItem();
            if (MouseUtil.isSingleClick(e)) {
                this.clearContextMenu();
            } else {
                if (item instanceof SSHConnectTreeItem treeItem) {
                    treeItem.connect();
                }
            }
        });
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof BaseTreeItem treeItem) {
                this.showContextMenu(treeItem.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // f2按键处理
        KeyboardListener.listenKeyReleased(this, KeyCode.F2, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof BaseTreeItem treeItem) {
                treeItem.rename();
            }
        });
        // 删除按键处理
        KeyboardListener.listenKeyReleased(this, KeyCode.DELETE, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof BaseTreeItem treeItem) {
                treeItem.delete();
            }
        });
        // 暂停按键处理
        KeyboardListener.listenKeyReleased(this, KeyCode.PAUSE, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof SSHConnectTreeItem treeItem) {
                treeItem.disConnect();
            }
        });
    }

    public void reload() {
    }
}
