package cn.oyzh.easyshell.exception.zk;

/**
 * zk节点无子节点创建权限异常
 *
 * @author oyzh
 * @since 2022/7/8
 */
public class ShellZKNoCreatePermException extends ShellZKNoAuthException {

    public ShellZKNoCreatePermException(String path) {
        super(path);
    }
}
