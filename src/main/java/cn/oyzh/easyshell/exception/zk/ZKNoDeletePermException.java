package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无删除权限异常
 *
 * @author oyzh
 * @since 2022/7/8
 */
public class ZKNoDeletePermException extends ZKNoAuthException {

    public ZKNoDeletePermException(String path) {
        super(path);
    }
}
