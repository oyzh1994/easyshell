package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * Redis哨兵操作异常
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisSentinelOperationException extends ShellException {

    /**
     * 构造函数
     */
    public ShellRedisSentinelOperationException() {
        this(I18nResourceBundle.i18nString("base.sentinel", "base.notSupport", "base.current", "base.operation"));
        // this("哨兵连接不支持此操作");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellRedisSentinelOperationException(String msg) {
        super(msg);
    }
}
