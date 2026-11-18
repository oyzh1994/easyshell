package cn.oyzh.easyshell.event.oracle.function;

import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle函数设计事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleFunctionDesignEvent extends Event<OracleFunction> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().getName();
    }

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
