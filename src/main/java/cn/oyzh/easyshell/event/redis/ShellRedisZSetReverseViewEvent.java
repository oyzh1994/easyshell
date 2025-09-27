package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.trees.redis.ShellRedisZSetKeyTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class ShellRedisZSetReverseViewEvent extends Event<ShellRedisZSetKeyTreeItem> {

    public Integer dbIndex() {
        return this.data().dbIndex();
    }

}
