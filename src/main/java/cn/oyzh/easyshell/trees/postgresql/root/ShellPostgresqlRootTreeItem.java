package cn.oyzh.easyshell.trees.postgresql.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeView;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL数据库树根节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlRootTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlRootTreeItemValue> {

    /**
     * 构造PostgreSQL数据库树根节点
     *
     * @param treeView 树视图
     */
    public ShellPostgresqlRootTreeItem(ShellPostgresqlTreeView treeView) {
        super(treeView);
        this.setValue(new ShellPostgresqlRootTreeItemValue());
    }

    /**
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient client() {
        return this.getTreeView().getClient();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect connect() {
        return this.client().getShellConnect();
    }

    /**
     * 判断模式是否存在
     *
     * @param dbName 模式名称
     * @return 是否存在
     */
    public boolean existSchema(String dbName) {
        return this.client().existSchema(dbName);
    }

    /**
     * 创建模式
     *
     * @param database 模式
     */
    public void createSchema(PostgresqlSchema database) {
        this.client().createSchema(database);
    }

    /**
     * 修改模式
     *
     * @param database 模式
     * @return 是否成功
     */
    public boolean alterSchema(PostgresqlSchema database) {
        return this.client().alterSchema(database);
    }

    /**
     * 删除模式
     *
     * @param dbName 模式名称
     * @return 是否成功
     */
    public boolean dropSchema(String dbName) {
        return this.client().dropSchema(dbName);
    }

    /**
     * 添加模式
     */
    @FXML
    private void addSchema() {
        StageAdapter adapter = ShellPostgresqlViewFactory.addSchema(this);
        if (adapter == null) {
            return;
        }
        String databaseName = adapter.getProp("databaseName");
        if (StringUtil.isNotBlank(databaseName)) {
            this.addDatabase(databaseName);
        }
    }

    /**
     * 添加模式节点
     *
     * @param schema 模式名称
     */
    public void addDatabase(String schema) {
        PostgresqlSchema dbSchema = this.client().schema(schema);
        super.addChild(new ShellPostgresqlSchemaTreeItem(dbSchema, this.getTreeView()));
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<PostgresqlSchema> schemas = this.client().selectSchemas();
        List<TreeItem<?>> list = new ArrayList<>();
        for (PostgresqlSchema schema : schemas) {
            list.add(new ShellPostgresqlSchemaTreeItem(schema, this.getTreeView()));
        }
        this.setChild(list);
        this.expend();
        this.doFilter();
        this.doSort();
    }

    @Override
    public void clearChild() {
        ObservableList<TreeItem<?>> children = this.unfilteredChildren();
        for (TreeItem<?> child : children) {
            if (child instanceof ShellPostgresqlSchemaTreeItem item) {
                item.closeDB();
            }
        }
        super.clearChild();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addSchema = MenuItemHelper.addSchema(this::addSchema);
        FXMenuItem reloadSchema = MenuItemHelper.reloadSchema(this::reloadChild);
        items.add(addSchema);
        items.add(reloadSchema);
        return items;
    }
}
