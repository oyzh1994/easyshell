package cn.oyzh.easyshell.exception;

/**
 * shell异常
 *
 * @author oyzh
 * @since 2025/03/21
 */
public class ShellException extends RuntimeException {

    /**
     * 构造函数
     */
    public ShellException() {
        super();
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public ShellException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param ex 异常原因
     */
    public ShellException(Throwable ex) {
        super(ex);
    }
}
