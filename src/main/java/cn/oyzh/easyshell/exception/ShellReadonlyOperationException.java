package cn.oyzh.easyshell.exception;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * 只读操作异常
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellReadonlyOperationException extends ShellException {

    /**
     * 构造函数
     */
    public ShellReadonlyOperationException() {
        this(I18nResourceBundle.i18nString("base.readonlyMode", "base.notSupport", "base.current", "base.operation"));
        // this("只读模式不支持此操作");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellReadonlyOperationException(String msg) {
        super(msg);
    }
}
