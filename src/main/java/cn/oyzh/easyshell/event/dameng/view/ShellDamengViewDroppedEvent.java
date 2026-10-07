package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦视图已删除事件
 *
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengViewDroppedEvent extends Event<ShellDamengViewTreeItem>   {

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellDamengSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
