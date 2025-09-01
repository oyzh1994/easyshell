package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.redis.key.RedisKeyAddedEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeyCopiedEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeyDeletedEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeyFilteredEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeyFlushedEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeyMovedEvent;
import cn.oyzh.easyshell.event.redis.key.RedisKeysMovedEvent;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.Objects;

/**
 * redis树
 *
 * @author oyzh
 * @since 2023/1/29
 */
public class RedisKeyTreeView extends RichTreeView implements FXEventListener {


    private final IntegerProperty dbIndexProperty = new SimpleIntegerProperty(0);

    public IntegerProperty dbIndexProperty() {
        return dbIndexProperty;
    }

    public Integer getDbIndex() {
        return this.dbIndexProperty.get();
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndexProperty.set(dbIndex);
    }

    private RedisClient client;

    public void setClient(RedisClient client) {
        this.client = client;
    }

    public RedisClient client() {
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
    protected void initRoot() {
        this.setRoot(new RedisKeyRootTreeItem(this));
//        super.setShowRoot(false);
        super.initRoot();
    }

    @Override
    public RedisKeyTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null && this.getDbIndex() != null) {
            RedisKeyTreeItemFilter filter = new RedisKeyTreeItemFilter();
            filter.initFilters(this.client().iid());
            this.itemFilter = filter;
        }
        return (RedisKeyTreeItemFilter) this.itemFilter;
    }

    @Override
    public RedisKeyRootTreeItem root() {
        return (RedisKeyRootTreeItem) super.root();
    }

    /**
     * 键添加事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyAdded(RedisKeyAddedEvent event) {
        if (event.data() == this.shellConnect() && event.getDbIndex() == this.getDbIndex()) {
            this.root().keyAdded(event.getKey());
        }
    }

    /**
     * 键删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyDeleted(RedisKeyDeletedEvent event) {
        if (event.data() == this.shellConnect() && event.getDbIndex() == this.getDbIndex()) {
            this.root().keyDeleted(event.getKey());
        }
    }

    /**
     * 键刷新事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyFlushed(RedisKeyFlushedEvent event) {
        if (Objects.equals(event.data(), this.getDbIndex())) {
            this.loadItems();
        }
    }

    /**
     * 键过滤事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyFiltered(RedisKeyFilteredEvent event) {
        if (Objects.equals(event.data(), this.getDbIndex())) {
            this.root().unloadChild();
            this.root().loadChild();
        }
    }

    /**
     * 键复制事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyCopied(RedisKeyCopiedEvent event) {
        int dbIndex = event.getTargetDB();
        if (dbIndex == this.getDbIndex()) {
            this.loadItems();
        }
    }

    /**
     * 键移动事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeyMoved(RedisKeyMovedEvent event) {
        // 检查连接
        if (event.shellConnect() != this.shellConnect()) {
            return;
        }
        // 目标库刷新节点
        if (event.getTargetDB() == this.getDbIndex()) {
            this.loadItems();
        } else if (event.sourceDB() == this.getDbIndex()) {// 来源库，移除此节点
            event.data().remove();
        }
    }

    /**
     * 多个键移动事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeysMoved(RedisKeysMovedEvent event) {
        // 检查连接
        if (event.redisConnect() != this.shellConnect()) {
            return;
        }
        // 来源库、目标库刷新节点
        if (event.getTargetDB() == this.getDbIndex() || event.sourceDB() == this.getDbIndex()) {
            this.loadItems();
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

    /**
     * 键过滤模式
     */
    private String filterPattern;

    public String getFilterPattern() {
        return filterPattern;
    }

    public void setFilterPattern(String filterPattern) {
        this.filterPattern = filterPattern;
    }
}
