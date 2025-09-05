package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2023/08/04
 */
public class ShellRedisClusterOperationException extends ShellException {

    public ShellRedisClusterOperationException() {
        this(I18nResourceBundle.i18nString("base.cluster", "base.notSupport", "base.current", "base.operation"));
        // this("Cluster集群不支持此操作");
    }

    public ShellRedisClusterOperationException(String msg) {
        super(msg);
    }
}
