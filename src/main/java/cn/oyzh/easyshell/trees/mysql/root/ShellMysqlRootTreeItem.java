package cn.oyzh.easyshell.trees.mysql.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeView;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlViewFactory;
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
 * mysql树根节点
 *
 * @author oyzh
 * @since 2023/06/16
 */
public class ShellMysqlRootTreeItem extends ShellMysqlTreeItem<ShellMysqlRootTreeItemValue> {

    /**
     * 构造mysql树根节点
     *
     * @param treeView 树视图
     */
    public ShellMysqlRootTreeItem(ShellMysqlTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMysqlRootTreeItemValue());
    }

    /**
     * 获取mysql客户端
     *
     * @return mysql客户端
     */
    public ShellMysqlClient client() {
        return this.getTreeView().getClient();
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect connect() {
        return this.client().getShellConnect();
    }

    /**
     * 数据库是否存在
     *
     * @param dbName 数据库名称
     * @return 是否存在
     */
    public boolean existDatabase(String dbName) {
        return this.client().existDatabase(dbName);
    }

    /**
     * 创建数据库
     *
     * @param database 数据库对象
     */
    public void createDatabase(MysqlDatabase database) {
        this.client().createDatabase(database);
    }

    /**
     * 修改数据库
     *
     * @param database 数据库对象
     * @return 是否修改成功
     */
    public boolean alterDatabase(MysqlDatabase database) {
        return this.client().alterDatabase(database);
    }

    /**
     * 获取数据库排序规则
     *
     * @param dbName 数据库名称
     * @return 排序规则
     */
    public String databaseCollation(String dbName) {
        return this.client().databaseCollation(dbName);
    }

    /**
     * 删除数据库
     *
     * @param dbName 数据库名称
     * @return 是否删除成功
     */
    public boolean dropDatabase(String dbName) {
        return this.client().dropDatabase(dbName);
    }

    /**
     * 新增数据库
     */
    @FXML
    private void addDatabase() {
        StageAdapter adapter = ShellMysqlViewFactory.addDatabase(this);
        if (adapter == null) {
            return;
        }
        String databaseName = adapter.getProp("databaseName");
        if (StringUtil.isNotBlank(databaseName)) {
            this.addDatabase(databaseName);
        }
    }

    /**
     * 新增数据库节点
     *
     * @param databaseName 数据库名称
     */
    public void addDatabase(String databaseName) {
        MysqlDatabase database = this.client().database(databaseName);
        super.addChild(new ShellMysqlDatabaseTreeItem(database, this.getTreeView()));
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        List<MysqlDatabase> databases = this.client().databases();
        List<TreeItem<?>> list = new ArrayList<>();
        for (MysqlDatabase database : databases) {
            list.add(new ShellMysqlDatabaseTreeItem(database, this.getTreeView()));
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
        FXMenuItem addDatabase = MenuItemHelper.addDatabase( this::addDatabase);
        FXMenuItem reloadDatabase = MenuItemHelper.reloadDatabase( this::reloadChild);
        items.add(addDatabase);
        items.add(reloadDatabase);
        return items;
    }
}
