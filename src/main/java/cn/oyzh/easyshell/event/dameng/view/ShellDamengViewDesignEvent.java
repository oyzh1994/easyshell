package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦视图设计事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengViewDesignEvent extends Event<DamengView> {

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
        return this.data().getName();
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
