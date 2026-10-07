package cn.oyzh.easyshell.exception;

import cn.oyzh.i18n.I18nHelper;

/**
 * 数据过大异常
 *
 * @author oyzh
 * @since 2025-09-05
 */
public class ShellDataTooBigException extends ShellException {

    /**
     * 构造函数
     */
    public ShellDataTooBigException() {
        this(I18nHelper.dataTooLarge());
        // this("数据太大");
    }

    /**
     * 构造函数
     *
     * @param msg 异常信息
     */
    public ShellDataTooBigException(String msg) {
        super(msg);
    }
}
