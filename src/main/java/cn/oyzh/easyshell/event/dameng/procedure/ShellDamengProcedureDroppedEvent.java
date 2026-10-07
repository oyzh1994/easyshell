package cn.oyzh.easyshell.event.dameng.procedure;

import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProcedureTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦存储过程已删除事件
 *
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengProcedureDroppedEvent extends Event<ShellDamengProcedureTreeItem>   {

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String procedureName() {
        return this.data().procedureName();
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
