package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无数据读取权限异常
 *
 * @author oyzh
 * @since 2023/03/06
 */
public class ShellZKNoReadPermException extends ShellZKNoAuthException {

    public ShellZKNoReadPermException(String path) {
        super(path);
    }
}
