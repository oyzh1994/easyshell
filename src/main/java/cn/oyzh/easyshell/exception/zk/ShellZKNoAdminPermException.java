package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无管理权限异常
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKNoAdminPermException extends ShellZKNoAuthException {

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoAdminPermException(String path) {
        super(path);
    }
}
