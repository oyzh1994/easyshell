package cn.oyzh.easyshell.event.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.user.ShellMongoUserTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb用户已删除事件
 *
 * @author oyzh
 * @since 2026-07-02
 */
public class ShellMongoUserDeletedEvent extends Event<ShellMongoUserTreeItem> implements EventFormatter {

    /**
     * 获取用户
     *
     * @return 用户
     */
    public MongoUser user() {
        return this.data().value();
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String userName() {
        return this.data().userName();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.user(), this.userName());
    }
}
