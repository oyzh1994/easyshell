package cn.oyzh.easyshell.event.oracle.view;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle视图已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleViewDroppedEvent extends Event<ShellOracleViewTreeItem>   {

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
    public ShellOracleSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
