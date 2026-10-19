package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.easyshell.redis.ShellRedisClient;

/**
 * redis订阅发布项目
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisPubsubItem {

    /** 获取编号 */
    public int getIndex() {
        return index;
    }

    /** 设置编号 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 获取通道 */
    public String getChannel() {
        return channel;
    }

    /** 设置通道 */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /** 获取redis客户端 */
    public ShellRedisClient getClient() {
        return client;
    }

    /** 设置redis客户端 */
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
