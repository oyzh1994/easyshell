package cn.oyzh.easyshell.exception.valkey;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.i18n.I18nResourceBundle;

/**
 * Valkey哨兵操作异常
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeySentinelOperationException extends ShellException {

    /**
     * 构造函数
     */
    public ShellValkeySentinelOperationException() {
        this(I18nResourceBundle.i18nString("base.sentinel", "base.notSupport", "base.current", "base.operation"));
        // this("哨兵连接不支持此操作");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellValkeySentinelOperationException(String msg) {
        super(msg);
    }
}
