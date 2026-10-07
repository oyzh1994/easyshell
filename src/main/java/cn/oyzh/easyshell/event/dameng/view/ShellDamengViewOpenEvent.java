package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦视图打开事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengViewOpenEvent extends Event<ShellDamengViewTreeItem> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
