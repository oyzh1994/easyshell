package cn.oyzh.easyshell.trees.mysql.root;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.trees.mysql.MysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.MysqlTreeView;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * DB树根节点
 *
 * @author oyzh
 * @since 2023/06/16
 */
public class MysqlRootTreeItem extends MysqlTreeItem<MysqlRootTreeItemValue> {

    public MysqlRootTreeItem(MysqlTreeView treeView) {
        super(treeView);
        this.setValue(new MysqlRootTreeItemValue());
    }

    public MysqlClient client() {
        return this.getTreeView().getClient();
    }

    public ShellConnect connect() {
        return this.client().getShellConnect();
    }

    public boolean existDatabase(String dbName) {
        return this.client().existDatabase(dbName);
    }

    public void createDatabase(MysqlDatabase database) {
        this.client().createDatabase(database);
    }

    public boolean alterDatabase(MysqlDatabase database) {
        return this.client().alterDatabase(database);
    }

    public String databaseCollation(String dbName) {
        return this.client().databaseCollation(dbName);
    }

    public boolean dropDatabase(String dbName) {
        return this.client().dropDatabase(dbName);
    }

    @Override
    public void loadChild() {
        List<MysqlDatabase> databases = this.client().databases();
        List<TreeItem<?>> list = new ArrayList<>();
        for (MysqlDatabase database : databases) {
            list.add(new MysqlDatabaseTreeItem(database, this.getTreeView()));
        }
        this.setChild(list);
        this.expend();
    }
}
