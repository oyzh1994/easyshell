package cn.oyzh.easyshell.trees.mongo.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoDatabase;
import cn.oyzh.easyshell.trees.mongo.MongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.MongoTreeView;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItemValue;
import cn.oyzh.easyshell.util.mongo.MongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
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
public class ShellMongoRootTreeItem extends MongoTreeItem<ShellMysqlRootTreeItemValue> {

    public ShellMongoRootTreeItem(MongoTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMysqlRootTreeItemValue());
    }

    public ShellMongoClient getClient() {
        return this.getTreeView().getClient();
    }

    public ShellConnect connect() {
        return this.getClient().getShellConnect();
    }

    public boolean existDatabase(String dbName) {
        return this.getClient().existDatabase(dbName);
    }

    public void createDatabase(String dbName) {
        this.getClient().createDatabase(dbName);
    }

    public boolean dropDatabase(String dbName) {
        return this.getClient().dropDatabase(dbName);
    }

    /**
     * 新增数据库
     */
    @FXML
    private void addDatabase() {
        StageAdapter adapter = MongoViewFactory.databaseAdd(this);
        if (adapter == null) {
            return;
        }
        String databaseName = adapter.getProp("databaseName");
        if (StringUtil.isNotBlank(databaseName)) {
            this.addDatabase(databaseName);
        }
    }

    public void addDatabase(String databaseName) {
        try {
            MongoDatabase database = this.getClient().database(databaseName);
            super.addChild(new MongoDatabaseTreeItem(database, this.getTreeView()));
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<MongoDatabase> databases = this.getClient().listDatabases();
        List<TreeItem<?>> list = new ArrayList<>();
        for (MongoDatabase database : databases) {
            list.add(new MongoDatabaseTreeItem(database, this.getTreeView()));
        }
        this.setChild(list);
        this.expend();
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
        FXMenuItem addDatabase = MenuItemHelper.addDatabase(this::addDatabase);
        FXMenuItem reloadDatabase = MenuItemHelper.reloadDatabase(this::reloadChild);
        items.add(addDatabase);
        items.add(reloadDatabase);
        return items;
    }
}
