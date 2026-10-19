package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦函数已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengFunctionDroppedEvent extends Event<ShellDamengFunctionTreeItem>   {

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().functionName();
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
