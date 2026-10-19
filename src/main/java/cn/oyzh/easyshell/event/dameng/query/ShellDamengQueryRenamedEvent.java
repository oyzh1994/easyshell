package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 达梦查询已重命名事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengQueryRenamedEvent extends Event<ShellDamengQueryTreeItem> implements EventFormatter {

    /**
     * 查询名称
     */
    private String queryName;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String newQueryName() {
        return this.data().queryName();
    }

    public String queryId() {
        return this.data().value().getUid();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.data().schema();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] renamed, new name:%s", I18nHelper.query(), this.getQueryName(), this.newQueryName());
    }
}
