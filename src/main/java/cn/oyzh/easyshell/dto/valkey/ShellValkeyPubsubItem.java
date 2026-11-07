package cn.oyzh.easyshell.dto.valkey;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;

/**
 * redis订阅发布项目
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyPubsubItem {

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
    public ShellValkeyClient getClient() {
        return client;
    }

    /** 设置redis客户端 */
    public void setClient(ShellValkeyClient client) {
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
    private ShellValkeyClient client;

}
