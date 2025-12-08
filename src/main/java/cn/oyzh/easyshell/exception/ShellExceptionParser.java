package cn.oyzh.easyshell.exception;

import java.util.function.Function;

/**
 * ssh异常解析器
 *
 * @author oyzh
 * @since 2020/7/2
 */
public class ShellExceptionParser implements Function<Throwable, String> {

    /**
     * 当前实例
     */
    public final static ShellExceptionParser INSTANCE = new ShellExceptionParser();

    @Override
    public String apply(Throwable e) {
        if (e == null) {
            return null;
        }

        // if (e instanceof RuntimeException) {
        //     if (e.getCause() != null) {
        //         e = e.getCause();
        //     }
        // }

        String message = e.getMessage();
        while (message == null) {
            e = e.getCause();
            if (e == null) {
                break;
            }
            message = e.getMessage();
        }
        if (e instanceof UnsupportedOperationException) {
            return message;
        }

        if (e instanceof IllegalArgumentException) {
            return message;
        }

        if (e != null) {
            e.printStackTrace();
        }

        return message;
    }
}
