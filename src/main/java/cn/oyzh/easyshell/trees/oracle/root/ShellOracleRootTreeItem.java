package cn.oyzh.easyshell.trees.oracle.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeItem;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.util.oracle.ShellOracleViewFactory;
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
 * Oracle数据库树根节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleRootTreeItem extends ShellOracleTreeItem<ShellOracleRootTreeItemValue> {

    /**
     * 构造Oracle数据库树根节点
     *
     * @param treeView 树视图
     */
    public ShellOracleRootTreeItem(ShellOracleTreeView treeView) {
        super(treeView);
        this.setValue(new ShellOracleRootTreeItemValue());
    }

    /**
     * 获取Oracle数据库客户端
     *
     * @return Oracle数据库客户端
     */
    public ShellOracleClient client() {
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
    public void createSchema(OracleSchema database) {
        this.client().createSchema(database);
    }

    /**
     * 修改模式
     *
     * @param database 模式
     * @return 是否成功
     */
    public boolean alterSchema(OracleSchema database) {
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
        StageAdapter adapter = ShellOracleViewFactory.addSchema(this);
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
        OracleSchema dbSchema = this.client().schema(schema);
        super.addChild(new ShellOracleSchemaTreeItem(dbSchema, this.getTreeView()));
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<OracleSchema> schemas = this.client().selectSchemas();
        List<TreeItem<?>> list = new ArrayList<>();
        for (OracleSchema schema : schemas) {
            list.add(new ShellOracleSchemaTreeItem(schema, this.getTreeView()));
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
            if (child instanceof ShellOracleSchemaTreeItem item) {
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
