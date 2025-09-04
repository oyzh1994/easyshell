package cn.oyzh.easyshell.exception.zk;

/**
 * zk节点无数据写入权限异常
 *
 * @author oyzh
 * @since 2022/7/8
 */
public class ZKNoWritePermException extends ZKNoAuthException {

    public ZKNoWritePermException(String path) {
        super(path);
    }
}
