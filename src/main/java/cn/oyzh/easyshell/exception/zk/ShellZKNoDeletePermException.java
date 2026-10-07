package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无删除权限异常
 *
 * @author oyzh
 * @since 2022/7/8
 */
public class ShellZKNoDeletePermException extends ShellZKNoAuthException {

    /**
     * 构造函数
     *
     * @param path 节点路径
     */
    public ShellZKNoDeletePermException(String path) {
        super(path);
    }
}
