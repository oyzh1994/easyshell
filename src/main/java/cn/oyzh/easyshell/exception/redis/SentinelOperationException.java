package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2023/08/06
 */
public class SentinelOperationException extends RedisException {

    public SentinelOperationException() {
        this(I18nResourceBundle.i18nString("base.sentinel", "base.notSupport", "base.current", "base.operation"));
        // this("哨兵连接不支持此操作");
    }

    public SentinelOperationException(String msg) {
        super(msg);
    }
}
