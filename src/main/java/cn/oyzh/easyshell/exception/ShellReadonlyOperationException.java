package cn.oyzh.easyshell.exception;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2023/12/09
 */
public class ShellReadonlyOperationException extends ShellException {

    public ShellReadonlyOperationException() {
        this(I18nResourceBundle.i18nString("base.readonlyMode", "base.notSupport", "base.current", "base.operation"));
        // this("只读模式不支持此操作");
    }

    public ShellReadonlyOperationException(String msg) {
        super(msg);
    }
}
