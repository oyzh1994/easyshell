package cn.oyzh.easyshell.event.dameng.function;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦函数已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengFunctionRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 新函数名称
     */
    private String newFunctionName;

    public String getNewFunctionName() {
        return newFunctionName;
    }

    public void setNewFunctionName(String newFunctionName) {
        this.newFunctionName = newFunctionName;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem.schema();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
