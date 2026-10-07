package cn.oyzh.easyshell.trees.dameng.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeView;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
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
 * 达梦数据库树根节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengRootTreeItem extends ShellDamengTreeItem<ShellDamengRootTreeItemValue> {

    /**
     * 构造达梦数据库树根节点
     *
     * @param treeView 树视图
     */
    public ShellDamengRootTreeItem(ShellDamengTreeView treeView) {
        super(treeView);
        this.setValue(new ShellDamengRootTreeItemValue());
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient client() {
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
    public void createSchema(DamengSchema database) {
        this.client().createSchema(database);
    }

    /**
     * 修改模式
     *
     * @param database 模式
     * @return 是否成功
     */
    public boolean alterSchema(DamengSchema database) {
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
        StageAdapter adapter = ShellDamengViewFactory.addSchema(this);
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
        DamengSchema dbSchema = this.client().schema(schema);
        super.addChild(new ShellDamengSchemaTreeItem(dbSchema, this.getTreeView()));
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<DamengSchema> schemas = this.client().selectSchemas();
        List<TreeItem<?>> list = new ArrayList<>();
        for (DamengSchema schema : schemas) {
            list.add(new ShellDamengSchemaTreeItem(schema, this.getTreeView()));
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
            if (child instanceof ShellDamengSchemaTreeItem item) {
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
