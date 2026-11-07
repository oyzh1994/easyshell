package cn.oyzh.easyshell.exception.valkey;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.i18n.I18nResourceBundle;

/**
 * Valkey集群操作异常
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyClusterOperationException extends ShellException {

    /**
     * 构造函数
     */
    public ShellValkeyClusterOperationException() {
        this(I18nResourceBundle.i18nString("base.cluster", "base.notSupport", "base.current", "base.operation"));
        // this("Cluster集群不支持此操作");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellValkeyClusterOperationException(String msg) {
        super(msg);
    }
}
