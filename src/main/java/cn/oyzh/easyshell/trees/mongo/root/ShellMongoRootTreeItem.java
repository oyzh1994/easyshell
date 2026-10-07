package cn.oyzh.easyshell.trees.mongo.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeView;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * mongodb树根节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoRootTreeItem extends ShellMongoTreeItem<ShellMongoRootTreeItemValue> {

    /**
     * 构造mongodb树根节点
     *
     * @param treeView 树视图
     */
    public ShellMongoRootTreeItem(ShellMongoTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMongoRootTreeItemValue());
    }

    /**
     * 获取mongodb客户端
     *
     * @return mongodb客户端
     */
    public ShellMongoClient getClient() {
        return this.getTreeView().getClient();
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect connect() {
        return this.getClient().getShellConnect();
    }

    /**
     * 数据库是否存在
     *
     * @param dbName 数据库名称
     * @return 是否存在
     */
    public boolean existDatabase(String dbName) {
        return this.getClient().existDatabase(dbName);
    }

    /**
     * 创建数据库
     *
     * @param dbName 数据库名称
     */
    public void createDatabase(String dbName) {
        this.getClient().createDatabase(dbName);
    }

    /**
     * 删除数据库
     *
     * @param dbName 数据库名称
     * @return 是否删除成功
     */
    public boolean dropDatabase(String dbName) {
        return this.getClient().dropDatabase(dbName);
    }

    /**
     * 新增数据库
     */
    @FXML
    private void addDatabase() {
        try {
            String databaseName = MessageBox.prompt(I18nHelper.pleaseInputDatabaseName());
            if (StringUtil.isBlank(databaseName)) {
                return;
            }
            if (this.existDatabase(databaseName)) {
                MessageBox.warn(I18nHelper.database() + " " + databaseName + " " + I18nHelper.alreadyExists());
                return;
            }
            this.createDatabase(databaseName);
            MongoDatabase database = this.getClient().database(databaseName);
            super.addChild(new ShellMongoDatabaseTreeItem(database, this.getTreeView()));
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
            list.add(new ShellMongoDatabaseTreeItem(database, this.getTreeView()));
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
            if (child instanceof ShellMongoDatabaseTreeItem item) {
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
