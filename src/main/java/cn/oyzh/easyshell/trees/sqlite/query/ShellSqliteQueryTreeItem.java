package cn.oyzh.easyshell.trees.sqlite.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQLite查询节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryTreeItem extends ShellSqliteTreeItem<ShellSqliteQueryTreeItemValue> {

    /**
     * 当前值
     */
    private final ShellQuery value;

    /**
     * 构造查询节点
     *
     * @param query    查询对象
     * @param treeView 树视图
     */
    public ShellSqliteQueryTreeItem(ShellQuery query, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = query;
        this.setValue(new ShellSqliteQueryTreeItemValue(this));
    }

    @Override
    public ShellSqliteQueriesTreeItem parent() {
        return (ShellSqliteQueriesTreeItem) super.parent();
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery value() {
        return this.value;
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
     * 获取查询名称
     *
     * @return 查询名称
     */
    public String queryName() {
        return this.value.getName();
    }

    /**
     * 刷新节点值
     */
    public void refreshValue() {
        this.refresh();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqliteEventUtil.queryOpen(this.client(), this.value);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openQuery = MenuItemHelper.openQuery(this::onPrimaryDoubleClick);
        items.add(openQuery);
        FXMenuItem renameQuery = MenuItemHelper.renameQuery(this::rename);
        items.add(renameQuery);
        FXMenuItem deleteQuery = MenuItemHelper.deleteQuery(this::delete);
        items.add(deleteQuery);
        return items;
    }

    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.queryName(), this.getWindow());
        if (name == null || Objects.equals(name, this.queryName())) {
            return;
        }
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        String oldName = this.value.getName();
        this.value.setName(name);
        if (ShellQueryStore.INSTANCE.update(this.value)) {
            ShellSqliteEventUtil.queryRenamed(this.client(), this.value, oldName);
            this.refreshValue();
        } else {
            this.value.setName(oldName);
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.delete() + " " + this.queryName() + "?", this.getWindow())) {
            return;
        }
        if (ShellQueryStore.INSTANCE.delete(this.value)) {
            ShellSqliteEventUtil.queryDeleted(this.client(), this.value);
            this.parent().clearQuerySize();
            this.remove();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }
}
