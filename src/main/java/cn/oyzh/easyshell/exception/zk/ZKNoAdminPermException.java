package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无管理权限异常
 *
 * @author oyzh
 * @since 2022/726
 */
public class ZKNoAdminPermException extends ZKNoAuthException {

    public ZKNoAdminPermException(String path) {
        super(path);
    }
}
