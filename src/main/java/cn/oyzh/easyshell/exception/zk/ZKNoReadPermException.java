package cn.oyzh.easyshell.exception.zk;


/**
 * zk节点无数据读取权限异常
 *
 * @author oyzh
 * @since 2023/03/06
 */
public class ZKNoReadPermException extends ZKNoAuthException {

    public ZKNoReadPermException(String path) {
        super(path);
    }
}
