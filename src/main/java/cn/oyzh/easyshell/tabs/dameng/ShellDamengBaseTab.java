package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * 达梦数据库基础标签页，提供数据库树节点、模式名称与连接名称等通用能力
 *
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellDamengBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellDamengSchemaTreeItem dbItem() ;

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.dbItem()==null?null: this.dbItem().schema();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.dbItem()==null?null: this.dbItem().connectName();
    }

}
