package cn.oyzh.easyshell.event.valkey;

import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyZSetKeyTreeItem;
import cn.oyzh.event.Event;

/**
 * redis zset反转视图事件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyZSetReverseViewEvent extends Event<ShellValkeyZSetKeyTreeItem> {

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public Integer dbIndex() {
        return this.data().dbIndex();
    }

}
