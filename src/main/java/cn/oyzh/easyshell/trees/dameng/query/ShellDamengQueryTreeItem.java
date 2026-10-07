package cn.oyzh.easyshell.trees.dameng.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
 * 达梦数据库树查询节点
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellDamengQueryTreeItem extends ShellDamengTreeItem<ShellDamengQueryTreeItemValue> {

    /**
     * 当前值
     */
    private final ShellQuery value;

    /**
     * 获取查询值
     *
     * @return 查询值
     */
    public ShellQuery value() {
        return value;
    }

    /**
     * 构造达梦数据库树查询节点
     *
     * @param query    查询
     * @param treeView 树视图
     */
    public ShellDamengQueryTreeItem(ShellQuery query, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = query;
        this.setValue(new ShellDamengQueryTreeItemValue(this));
    }

    @Override
    public ShellDamengQueriesTreeItem parent() {
        return (ShellDamengQueriesTreeItem) super.parent();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellDamengClient client() {
        return this.parent().client();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
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
                ShellDamengEventUtil.queryDeleted(this);
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
            ShellDamengEventUtil.queryRenamed(this.value.getUid(), oldName, name, this.dbItem());
            this.refresh();
        } else {
            this.value.setName(oldName);
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellDamengSchemaTreeItem dbItem() {
        return this.parent().parent();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
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
        ShellDamengEventUtil.queryOpen(this.value, this.dbItem());
    }
}
