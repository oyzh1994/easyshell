package cn.oyzh.easyshell.exception.mongo;

import java.util.function.Function;

/**
 * redis异常信息解析
 *
 * @author oyzh
 * @since 2023/7/2
 */
public class MongoExceptionParser implements Function<Throwable, String> {

    /**
     * 当前实例
     */
    public final static MongoExceptionParser INSTANCE = new MongoExceptionParser();

    @Override
    public String apply(Throwable e) {
        if (e == null) {
            return null;
        }

        if (e instanceof MongoException) {
            return e.getMessage();
        }

        if (e instanceof RuntimeException) {
            if (e.getCause() != null) {
                e = e.getCause();
            }
        }

        String message = e.getMessage();

        if (e instanceof MongoException) {
            return message;
        }

        if (e instanceof UnsupportedOperationException) {
            return message;
        }

        if (e instanceof IllegalArgumentException) {
            return message;
        }

        e.printStackTrace();
        return message;
    }
}
