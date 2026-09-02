package cn.oyzh.easyshell.trees.dameng.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.trees.dameng.DBTreeItem;
import cn.oyzh.easyshell.trees.dameng.DBTreeView;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
import cn.oyzh.fx.db.DBSchema;
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
 * DB树根节点
 *
 * @author oyzh
 * @since 2023/06/16
 */
public class DBRootTreeItem extends DBTreeItem<DBRootTreeItemValue> {

    public DBRootTreeItem(DBTreeView treeView) {
        super(treeView);
        this.setValue(new DBRootTreeItemValue());
    }

    public ShellDamengClient client() {
        return this.getTreeView().getClient();
    }

    public ShellConnect connect() {
        return this.client().getShellConnect();
    }

    public boolean existSchema(String dbName) {
        return this.client().existSchema(dbName);
    }

    public void createSchema(DamengSchema database) {
        this.client().createSchema(database);
    }

    public boolean alterSchema(DamengSchema database) {
        return this.client().alterSchema(database);
    }

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

    public void addDatabase(String schema) {
        DamengSchema dbSchema = this.client().schema(schema);
        super.addChild(new DamengSchemaTreeItem(dbSchema, this.getTreeView()));
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<DamengSchema> schemas = this.client().schemas();
        List<TreeItem<?>> list = new ArrayList<>();
        for (DamengSchema schema : schemas) {
            list.add(new DamengSchemaTreeItem(schema, this.getTreeView()));
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
            if (child instanceof ShellMysqlDatabaseTreeItem item) {
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
