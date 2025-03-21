package cn.oyzh.easyshell.exception;

/**
 * shell异常
 *
 * @author oyzh
 * @since 2025/03/21
 */
public class ShellException extends RuntimeException {

    public ShellException() {
        super();
    }

    public ShellException(String message) {
        super(message);
    }

    public ShellException(Exception ex) {
        super(ex);
    }
}
