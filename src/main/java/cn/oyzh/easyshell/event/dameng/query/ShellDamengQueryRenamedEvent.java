package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 达梦查询已重命名事件
 *
 * @author oyzh
 * @since 2024/01/23
 */
public class ShellDamengQueryRenamedEvent extends Event<String> implements EventFormatter {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 查询名称
     */
    private String queryName;

    /**
     * 新查询名称
     */
    private String newQueryName;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getNewQueryName() {
        return newQueryName;
    }

    public void setNewQueryName(String newQueryName) {
        this.newQueryName = newQueryName;
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

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.query(), this.getQueryName(), this.getNewQueryName());
    }
}
