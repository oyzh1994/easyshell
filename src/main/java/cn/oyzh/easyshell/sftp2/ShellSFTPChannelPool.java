package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.util.Pool;

/**
 * sftp通道管理器
 *
 * @author oyzh
 * @since 2025-06-07
 */
public class ShellSFTPChannelPool extends Pool<ShellSFTPChannel> implements AutoCloseable {

    /**
     * 客户端
     */
    private ShellSFTPClient client;

    public ShellSFTPChannelPool(ShellSFTPClient client) {
        super(1, 5);
        this.client = client;
        super.setWaitingBorrow(true);
    }

    @Override
    public void returnObject(ShellSFTPChannel channel) {
        if (channel == null || channel.isClosed()) {
            return;
        }
        if (this.isFull()) {
            channel.close();
            return;
        }
        super.returnObject(channel);
    }

    @Override
    protected ShellSFTPChannel newObject() throws Exception {
        return this.client.newSFTPChannel();
    }

    @Override
    public void close() throws Exception {
        for (ShellSFTPChannel channel : this.list()) {
            channel.close();
        }
        super.clear();
        this.client = null;
    }
}
