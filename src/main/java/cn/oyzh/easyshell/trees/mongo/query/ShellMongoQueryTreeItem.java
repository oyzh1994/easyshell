package cn.oyzh.easyshell.trees.mongo.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mongodb树查询节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoQueryTreeItem extends ShellMongoTreeItem<ShellMongoQueryTreeItemValue> {

    /**
     * 当前值
     */
    private final ShellQuery value;

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery value() {
        return value;
    }

    /**
     * 构造查询节点
     *
     * @param query    查询对象
     * @param treeView 树视图
     */
    public ShellMongoQueryTreeItem(ShellQuery query, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = query;
        this.setValue(new ShellMongoQueryTreeItemValue(this));
    }

    @Override
    public ShellMongoQueriesTreeItem parent() {
        return (ShellMongoQueriesTreeItem) super.parent();
    }

    /**
     * 获取mongodb客户端
     *
     * @return mongodb客户端
     */
    public ShellMongoClient client() {
        return this.parent().client();
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
        FXMenuItem openQuery = MenuItemHelper.openQuery(this::onPrimaryDoubleClick);
        items.add(openQuery);
        FXMenuItem renameQuery = MenuItemHelper.renameQuery(this::rename);
        items.add(renameQuery);
        FXMenuItem deleteQuery = MenuItemHelper.deleteQuery(this::delete);
        items.add(deleteQuery);
        return items;
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " " + this.queryName() + "?")) {
            if (ShellQueryStore.INSTANCE.delete(this.value)) {
                ShellMongoEventUtil.queryDeleted(this);
                this.parent().clearQuerySize();
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        }
    }

    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.queryName());
        // 名称为null或者跟当前名称相同，则忽略
        if (name == null || Objects.equals(name, this.queryName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nHelper.pleaseInputName());
            return;
        }
        String oldName = this.value.getName();
        this.value.setName(name);
        // 修改名称
        if (ShellQueryStore.INSTANCE.update(this.value)) {
            ShellMongoEventUtil.queryRenamed(this, oldName);
            this.refresh();
        } else {
            this.value.setName(oldName);
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 获取所属数据库节点
     *
     * @return 数据库节点
     */
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.parent().parent();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取查询名称
     *
     * @return 查询名称
     */
    public String queryName() {
        return this.value.getName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.queryOpen(this.value, this.dbItem());
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.client().getShellConnect();
    }
}
