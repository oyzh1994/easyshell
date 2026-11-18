package cn.oyzh.easyshell.event.oracle.view;

import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewTreeItem;
import cn.oyzh.event.Event;

/**
 * Oracle视图打开事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleViewOpenEvent extends Event<ShellOracleViewTreeItem> {

    /**
     * 数据库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.data().viewName();
    }

    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellOracleSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
