package cn.oyzh.easyshell.exception.redis;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.function.Function;

/**
 * redis异常信息解析
 *
 * @author oyzh
 * @since 2023/7/2
 */
public class RedisExceptionParser implements Function<Throwable, String> {

    /**
     * 当前实例
     */
    public final static RedisExceptionParser INSTANCE = new RedisExceptionParser();

    @Override
    public String apply(Throwable e) {
        if (e == null) {
            return null;
        }

        if (e instanceof RuntimeException) {
            if (e.getCause() != null) {
                e = e.getCause();
            }
        }

        // if (e instanceof SSHException e1) {
        //     if (StrUtil.contains(e.getMessage(), "Auth fail")) {
        //         // return "ssh认证失败，请检查ssh用户名、密码是否正确";
        //         return I18nResourceBundle.i18nString("base.ssh", "base.authFail");
        //     }
        //     return e1.getMessage();
        // }

        String message = e.getMessage();
        if (e instanceof JedisDataException) {
            // if (StrUtil.contains(message, "NOAUTH Authentication required")) {
            //     return "连接需要认证！";
            // }
            // if (StrUtil.contains(message, "ERR invalid longitude")) {
            //     return "坐标经纬度参数错误！";
            // }
            // if (StrUtil.contains(message, "ERR The ID specified in XADD is equal or smaller than the target stream top item")) {
            //     return "消息ID值过小！";
            // }
            // if (StrUtil.contains(message, "ERR source and destination objects are the same")) {
            //     return "来源库和目标库相同！";
            // }
            // if (StrUtil.contains(message, "READONLY You can't write against a read only replica")) {
            //     return "当前是只读副本连接(从节点)！";
            // }
            // if (StrUtil.contains(message, "ERR invalid password")) {
            //     return "认证密码错误！";
            // }
            return message;
        }

        if (e instanceof JedisConnectionException) {
            // if (StrUtil.containsAny(message, "Attempting to read from a broken connection", "你的主机中的软件中止了一个已建立的连接")) {
            //     return "连接已中断！";
            // }
            // if (StrUtil.contains(message, "Failed to connect to")) {
            //     return "连接失败，请检查Redis服务是否启动、网络是否可用、认证信息是否正确";
            // }
            // if (StrUtil.contains(message, "Read timed out")) {
            //     return "连接失败，读取超时";
            // }
            return message;
        }

        if (e instanceof JedisException) {
            return message;
        }

        if (e instanceof UnsupportedOperationException) {
            return message;
        }

        if (e instanceof IllegalArgumentException) {
            return message;
        }

        if (e instanceof RedisException) {
            return message;
        }

        e.printStackTrace();
        return message;
    }
}
