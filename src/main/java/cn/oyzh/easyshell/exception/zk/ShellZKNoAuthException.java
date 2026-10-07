package cn.oyzh.easyshell.exception.zk;

import org.apache.zookeeper.KeeperException;

/**
 * zk无权限异常
 *
 * @author oyzh
 * @since 2023/5/31
 */
public class ShellZKNoAuthException extends KeeperException.NoAuthException {

    /**
     * 节点路径
     */
    protected String path;

    @Override
    public String getPath() {
        return path;
    }

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoAuthException(String path) {
        this.path = path;
    }

}
