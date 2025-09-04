package cn.oyzh.easyshell.exception.zk;

import org.apache.zookeeper.KeeperException;

/**
 * zk无权限异常
 *
 * @author oyzh
 * @since 2023/5/31
 */
public class ZKNoAuthException extends KeeperException.NoAuthException {

    protected String path;

    @Override
    public String getPath() {
        return path;
    }

    public ZKNoAuthException(String path) {
        this.path = path;
    }

}
