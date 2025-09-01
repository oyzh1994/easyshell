package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2023/12/09
 */
public class ReadonlyOperationException extends RedisException {

    public ReadonlyOperationException() {
        this(I18nResourceBundle.i18nString("base.readonlyMode", "base.notSupport", "base.current", "base.operation"));
        // this("只读模式不支持此操作");
    }

    public ReadonlyOperationException(String msg) {
        super(msg);
    }
}
