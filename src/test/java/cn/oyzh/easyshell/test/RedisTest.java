package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.RedisClient;
import org.junit.Test;

/**
 * @author oyzh
 * @since 2025-09-03
 */
public class RedisTest {

    @Test
    public void test1() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setHost("127.0.0.1:6379");
        connect.setPassword("123456");
        RedisClient redisClient = new RedisClient(connect);
        redisClient.start();
        redisClient.set(0, "s1", "123455");
        System.out.println(redisClient.get(0, "s1"));
    }

    @Test
    public void test2() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setHost("127.0.0.1:8380");
        connect.setPassword("123456");
        connect.setSSLMode(true);
        RedisClient redisClient = new RedisClient(connect);
        redisClient.start();
        redisClient.set(0, "s1", "123455");
        System.out.println(redisClient.get(0, "s1"));
    }
}
