package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.trees.redis.key.ShellRedisZSetKeyTreeItem;
import cn.oyzh.event.Event;

/**
 * redis zset反转视图事件
 *
 * @author oyzh
 * @since 2024/5/17
 */
public class ShellRedisZSetReverseViewEvent extends Event<ShellRedisZSetKeyTreeItem> {

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public Integer dbIndex() {
        return this.data().dbIndex();
    }

}
