package cn.oyzh.easyshell.event.oracle.view;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle视图已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleViewRenamedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 新视图名称
     */
    private String newViewName;

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
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

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
