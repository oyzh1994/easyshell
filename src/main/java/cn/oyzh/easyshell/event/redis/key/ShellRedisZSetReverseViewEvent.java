package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class ShellRedisZSetReverseViewEvent extends Event<RedisZSetKeyTreeItem> {

    public Integer dbIndex() {
        return this.data().dbIndex();
    }

}
