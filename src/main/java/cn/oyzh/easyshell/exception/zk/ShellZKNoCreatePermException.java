package cn.oyzh.easyshell.exception.zk;

/**
 * zk节点无子节点创建权限异常
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKNoCreatePermException extends ShellZKNoAuthException {

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoCreatePermException(String path) {
        super(path);
    }
}
