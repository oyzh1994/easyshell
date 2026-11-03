package cn.oyzh.easyshell.trees.sqlite.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite视图类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewsTreeItem extends ShellSqliteTreeItem<ShellSqliteViewsTreeItemValue> {

    /**
     * 视图数量缓存
     */
    private Integer viewSize;

    /**
     * 构造视图类型节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteViewsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellSqliteViewsTreeItemValue(this));
    }

    @Override
    public ShellSqliteRootTreeItem parent() {
        return (ShellSqliteRootTreeItem) super.parent();
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.parent().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
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

    /**
     * 获取视图数量
     *
     * @return 视图数量
     */
    public int viewSize() {
        try {
            return this.client().viewSize(this.dbName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取视图数量（带缓存）
     *
     * @return 视图数量
     */
    public int getViewSize() {
        if (this.viewSize == null) {
            this.viewSize = this.viewSize();
        }
        return this.viewSize;
    }

    /**
     * 清空视图数量缓存
     */
    public void clearViewSize() {
        this.viewSize = null;
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
            for (SQLiteView view : this.client().selectViews()) {
                items.add(new ShellSqliteViewTreeItem(view, this.getTreeView()));
            }
            this.setChild(items);
            this.clearViewSize();
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } catch (Exception ex) {
            this.setLoaded(false);
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * 新增视图并刷新排序
     *
     * @param view 视图对象
     */
    public void addView(SQLiteView view) {
        if (view == null || StringUtil.isBlank(view.getName())) {
            return;
        }
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellSqliteViewTreeItem item
                    && StringUtil.equals(item.viewName(), view.getName())) {
                this.refreshCount();
                return;
            }
        }
        this.addChild(new ShellSqliteViewTreeItem(view, this.getTreeView()));
        this.doFilter();
        this.doSort();
        this.expend();
        this.refreshCount();
    }

    /**
     * 刷新视图数量
     */
    public void refreshCount() {
        this.clearViewSize();
        this.refresh();
    }

    @Override
    public void reloadChild() {
        this.clearViewSize();
        this.clearChild();
        this.setLoaded(false);
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

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addView(() -> ShellSqliteEventUtil.designView(this.client(), null));
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }
}
