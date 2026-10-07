package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;

/**
 * redis键刷新事件
 *
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisKeyFlushedEvent extends Event<Integer> {

    /**
     * 连接
     */
    private ShellConnect connect;

    public ShellConnect getConnect() {
        return connect;
    }

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }
}
