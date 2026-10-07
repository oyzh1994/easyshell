package cn.oyzh.easyshell.exception.zk;

/**
 * zk节点无数据写入权限异常
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKNoWritePermException extends ShellZKNoAuthException {

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoWritePermException(String path) {
        super(path);
    }
}
