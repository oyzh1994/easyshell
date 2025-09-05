package cn.oyzh.easyshell.exception;

import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/8/14
 */
public class ShellDataTooBigException extends ShellException {

    public ShellDataTooBigException() {
        this(I18nHelper.dataTooLarge());
        // this("数据太大");
    }

    public ShellDataTooBigException(String msg) {
        super(msg);
    }
}
