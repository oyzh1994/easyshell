package cn.oyzh.easyshell.trees.h2.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2查询类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueriesTreeItem extends ShellH2TreeItem<ShellH2QueriesTreeItemValue> {

    /**
     * 构造查询类型节点
     *
     * @param treeView 树视图
     */
    public ShellH2QueriesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellH2QueriesTreeItemValue(this));
    }

    @Override
    public ShellH2SchemaTreeItem parent() {
        return (ShellH2SchemaTreeItem) super.parent();
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
        return this.parent().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().schema();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addQuery = MenuItemHelper.addQuery(this::addQuery);
        items.add(addQuery);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增查询
     */
    private void addQuery() {
        ShellQuery query = new ShellQuery();
        query.setDbName(this.dbName());
        ShellH2EventUtil.queryOpen(this.client(), query);
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        try {
            this.setLoading(true);
            this.clearChild();
            List<TreeItem<?>> items = new ArrayList<>();
            List<ShellQuery> queries = ShellQueryStore.INSTANCE.list(
                    this.info().getId(), this.dbName());
            if (StringUtil.equalsIgnoreCase(this.dbName(), this.client().schema())) {
                List<ShellQuery> legacy = ShellQueryStore.INSTANCE.list(
                        this.info().getId(), "main");
                if (legacy != null) {
                    for (ShellQuery query : legacy) {
                        query.setDbName(this.dbName());
                        ShellQueryStore.INSTANCE.update(query);
                    }
                    List<ShellQuery> merged = new ArrayList<>();
                    if (queries != null) {
                        merged.addAll(queries);
                    }
                    merged.addAll(legacy);
                    queries = merged.stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    ShellQuery::getUid, item -> item,
                                    (left, right) -> left, java.util.LinkedHashMap::new))
                            .values().stream().toList();
                }
            }
            if (queries != null) {
                for (ShellQuery query : queries) {
                    items.add(new ShellH2QueryTreeItem(query, this.getTreeView()));
                }
            }
            this.setChild(items);
            this.clearQuerySize();
            this.doFilter();
            this.doSort();
            this.setLoaded(true);
            this.expend();
            this.refresh();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * 新增或更新查询子节点
     *
     * @param query 查询对象
     */
    public void upsertQuery(ShellQuery query) {
        if (query == null) {
            return;
        }
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellH2QueryTreeItem item
                    && StringUtil.equals(item.value().getUid(), query.getUid())) {
                // 树节点可能和已打开标签页持有不同的查询实例，需要整体同步，避免重新打开时读到旧内容
                ShellQuery value = item.value();
                value.setName(query.getName());
                value.setContent(query.getContent());
                value.setIid(query.getIid());
                value.setDbName(query.getDbName());
                value.setDbIndex(query.getDbIndex());
                item.refreshValue();
                this.doSort();
                this.clearQuerySize();
                this.refresh();
                return;
            }
        }
        this.addChild(new ShellH2QueryTreeItem(query, this.getTreeView()));
        this.doFilter();
        this.doSort();
        this.expend();
        this.clearQuerySize();
        this.refresh();
    }

    /**
     * 删除查询子节点
     *
     * @param query 查询对象
     */
    public void removeQuery(ShellQuery query) {
        if (query == null) {
            return;
        }
        for (TreeItem<?> child : new ArrayList<>(this.unfilteredChildren())) {
            if (child instanceof ShellH2QueryTreeItem item
                    && StringUtil.equals(item.value().getUid(), query.getUid())) {
                this.removeChild(item);
                break;
            }
        }
        this.doFilter();
        this.clearQuerySize();
        this.refresh();
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.clearQuerySize();
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 获取查询数量
     *
     * @return 查询数量
     */
    public int querySize() {
        try {
            List<ShellQuery> queries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.dbName());
            return queries == null ? 0 : queries.size();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 查询数量缓存
     */
    private Integer querySize;

    /**
     * 获取查询数量（带缓存）
     *
     * @return 查询数量
     */
    public Integer getQuerySize() {
        if (this.querySize == null) {
            this.querySize = this.querySize();
        }
        return this.querySize;
    }

    /**
     * 新增查询并刷新排序
     *
     * @param query 查询对象
     */
    public void addQuery(ShellQuery query) {
        if (StringUtil.isBlank(query.getDbName())) {
            query.setDbName(this.dbName());
        }
        this.addChild(new ShellH2QueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearQuerySize();
    }

    /**
     * 清空查询数量缓存
     */
    public void clearQuerySize() {
        this.querySize = null;
    }
}
