package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * Redis集群操作异常
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisClusterOperationException extends ShellException {

    /**
     * 构造函数
     */
    public ShellRedisClusterOperationException() {
        this(I18nResourceBundle.i18nString("base.cluster", "base.notSupport", "base.current", "base.operation"));
        // this("Cluster集群不支持此操作");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellRedisClusterOperationException(String msg) {
        super(msg);
    }
}
