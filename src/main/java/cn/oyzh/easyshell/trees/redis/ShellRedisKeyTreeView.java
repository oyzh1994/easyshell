package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyFlushedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeysCopiedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeysMovedEvent;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * redis树
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class ShellRedisKeyTreeView extends RichTreeView implements FXEventListener {

    private ShellRedisClient client;

    public void setClient(ShellRedisClient client) {
        this.client = client;
        FXUtil.runWait(() -> {
            if (client.isClusterMode()) {
                this.setRoot(new ShellRedisDatabaseTreeItem(null, this));
            } else {
                this.setRoot(new ShellRedisKeyRootTreeItem(this));
            }
        });
    }

    public ShellRedisClient getClient() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.shellConnect();
    }

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    public ShellRedisKeyTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            ShellRedisKeyTreeItemFilter filter = new ShellRedisKeyTreeItemFilter();
            // filter.initFilters(this.getClient().iid());
            this.itemFilter = filter;
        }
        return (ShellRedisKeyTreeItemFilter) this.itemFilter;
    }

    // @Override
    // public ShellRedisKeyRootTreeItem root() {
    //     return (ShellRedisKeyRootTreeItem) super.root();
    // }

    // /**
    //  * 键添加事件
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // private void keyAdded(ShellRedisKeyAddedEvent event) {
    //     if (event.data() == this.shellConnect() && event.getDbIndex() == this.getDbIndex()) {
    //         this.root().keyAdded(event.getKey());
    //     }
    // }

    // /**
    //  * 键删除事件
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // private void keyDeleted(ShellRedisKeyDeletedEvent event) {
    //     if (event.data() == this.shellConnect() && event.getDbIndex() == this.getDbIndex()) {
    //         this.root().keyDeleted(event.getKey());
    //     }
    // }

    public List<ShellRedisDatabaseTreeItem> dbItems() {
        List<TreeItem<?>> list = this.root().getChildren();
        List<ShellRedisDatabaseTreeItem> items = new ArrayList<>();
        for (TreeItem<?> item : list) {
            if (item instanceof ShellRedisDatabaseTreeItem) {
                items.add((ShellRedisDatabaseTreeItem) item);
            }
        }
        return items;
    }

    /**
     * 键刷新事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeyFlushed(ShellRedisKeyFlushedEvent event) {
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
            if (Objects.equals(item.dbIndex(), event.data())) {
                item.reloadChild();
                break;
            }
        }
    }

    // /**
    //  * 键过滤事件
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // private void keyFiltered(RedisKeyFilteredEvent event) {
    //     if (Objects.equals(event.data(), this.getDbIndex())) {
    //         this.root().unloadChild();
    //         this.root().loadChild();
    //     }
    // }

    /**
     * 键复制事件
     *
     * @param dbIndex 目标库
     */
    public void keyCopied(int dbIndex) {
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
            if (dbIndex == item.dbIndex()) {
                item.reloadChild();
                break;
            }
        }
    }

    /**
     * 键移动事件
     *
     * @param dbIndex 目标库
     */
    public void keyMoved(int dbIndex) {
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
            if (dbIndex == item.dbIndex()) {
                item.reloadChild();
                break;
            }
        }
    }

    /**
     * 多个键复制事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeysCopied(ShellRedisKeysCopiedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 目标库刷新节点
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
            if (event.getTargetDB() == item.dbIndex()) {
                item.reloadChild();
                break;
            }
        }
    }

    /**
     * 多个键移动事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeysMoved(ShellRedisKeysMovedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 来源库、目标库刷新节点
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
            if (event.getTargetDB() == item.dbIndex() || event.getSourceDB() == item.dbIndex()) {
                item.reloadChild();
            }
        }
    }

    /**
     * 加载节点
     */
    public void loadItems() {
        this.disable();
        try {
            this.root().loadChild();
        } finally {
            this.enable();
        }
    }

    @Override
    public synchronized void sortAsc() {
        RichTreeItem<?> item = this.getSelectedItem();
        this.root().sortAsc();
        if (item != null) {
            this.select(item);
        }
        this.refresh();
    }

    @Override
    public synchronized void sortDesc() {
        RichTreeItem<?> item = this.getSelectedItem();
        this.root().sortDesc();
        if (item != null) {
            this.select(item);
        }
        this.refresh();
    }

    // /**
    //  * 键过滤模式
    //  */
    // private String filterPattern;
    //
    // public String getFilterPattern() {
    //     return filterPattern;
    // }
    //
    // public void setFilterPattern(String filterPattern) {
    //     this.filterPattern = filterPattern;
    // }
}
