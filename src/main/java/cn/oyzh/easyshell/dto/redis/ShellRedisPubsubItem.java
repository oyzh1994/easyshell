package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.easyshell.redis.ShellRedisClient;

/**
 * redis订阅发布项目
 *
 * @author oyzh
 * @since 2023/8/02
 */
public class ShellRedisPubsubItem {

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public ShellRedisClient getClient() {
        return client;
    }

    public void setClient(ShellRedisClient client) {
        this.client = client;
    }

    /**
     * 编号
     */
    private int index;

    /**
     * 通道
     */
    private String channel;

    /**
     * redis客户端
     */
    private ShellRedisClient client;

}
