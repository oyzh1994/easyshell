package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无子节点权限异常
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKNoChildPermException extends ShellZKNoAuthException {

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoChildPermException(String path) {
        super(path);
    }
}
