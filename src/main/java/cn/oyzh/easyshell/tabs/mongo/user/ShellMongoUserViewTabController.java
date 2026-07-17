package cn.oyzh.easyshell.tabs.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.mongo.user.MongoUserRole;
import cn.oyzh.easyshell.mongo.user.MongoUserRoleDb;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.HashMap;
import java.util.Map;

/**
 * mongodb函数设计tab
 *
 * @author oyzh
 * @since 2024/07/08
 */
public class ShellMongoUserViewTabController extends RichTabController {

    /**
     * 用户名
     */
    @FXML
    private ReadOnlyTextField user;

    /**
     * 数据库
     */
    @FXML
    private ReadOnlyTextField database;

    /**
     * 角色
     */
    @FXML
    private FXTableView<MongoUserRoleDb> roleTableView;

    /**
     * mongo用户
     */
    private MongoUser mongoUser;

    /**
     * db节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    public MongoUser getMongoUser() {
        return mongoUser;
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void init(MongoUser user, ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.mongoUser = user;
        Map<String, MongoUserRoleDb> dbs = new HashMap<>();
        for (MongoUserRole role : user.getRoles()) {
            MongoUserRoleDb roleDb;
            if (dbs.containsKey(role.getDb())) {
                roleDb = dbs.get(role.getDb());
            } else {
                roleDb = new MongoUserRoleDb();
                roleDb.setDb(role.getDb());
                dbs.put(role.getDb(), roleDb);
            }
            roleDb.roles().add(role.getRole());
        }
        this.user.setText(user.getUser());
        this.database.setText(user.getDb());
        this.roleTableView.setItem(dbs.values());
    }
}
