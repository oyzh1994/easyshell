package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦函数设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengFunctionDesignEvent extends Event<DamengFunction> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
