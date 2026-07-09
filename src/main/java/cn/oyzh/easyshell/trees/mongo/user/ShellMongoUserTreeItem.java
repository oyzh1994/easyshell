package cn.oyzh.easyshell.trees.mongo.user;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * mongodb树集合节点
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellMongoUserTreeItem extends ShellMongoTreeItem<ShellMongoUserTreeItemValue> {

    /**
     * 当前值
     */
    private final MongoUser value;

    public ShellMongoUserTreeItem(MongoUser user, RichTreeView treeView) {
        super(treeView);
        this.value = user;
        this.setValue(new ShellMongoUserTreeItemValue(this));
    }

    @Override
    public ShellMongoUsersTreeItem parent() {
        return (ShellMongoUsersTreeItem) super.parent();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public String userName() {
        return this.value.getUser();
    }

    /**
     * 获取mongodb信息
     *
     * @return mongodb信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem viewUser = MenuItemHelper.view1User(this::viewUser);
        FXMenuItem deleteUser = MenuItemHelper.deleteUser(this::delete);
        items.add(viewUser);
        items.add(deleteUser);
        return items;
    }

    /**
     * 查看用户
     */
    private void viewUser() {
        ShellMongoViewFactory.viewUser(this.value);
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(I18nHelper.deleteUser() + "[" + this.userName() + "]")) {
                this.dbItem().dropUser(this.userName());
                ShellMongoEventUtil.userDeleted(this);
                this.parent().clearUserSize();
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public ShellMongoDatabaseTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    public String infoName() {
        return parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
      this.viewUser();
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public MongoUser value() {
        return value;
    }
}
