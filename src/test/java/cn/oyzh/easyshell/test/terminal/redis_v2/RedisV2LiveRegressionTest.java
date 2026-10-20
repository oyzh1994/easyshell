package cn.oyzh.easyshell.test.terminal.redis_v2;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisCommandSession;
import cn.oyzh.easyshell.terminal.redis_v2.RedisCommandBuilder;
import cn.oyzh.easyshell.terminal.redis_v2.RedisCommandExecutor;
import cn.oyzh.easyshell.terminal.redis_v2.RedisCommandRegistry;
import cn.oyzh.easyshell.terminal.redis_v2.RedisResponseFormatter;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 可选的本地 Redis v2 端到端回归测试。
 */
public class RedisV2LiveRegressionTest {

    @Test
    public void executesDynamicRedisCommands() throws Exception {
        Assume.assumeTrue("Enable with -Dredis.v2.live=true",
                Boolean.getBoolean("redis.v2.live"));
        String host = System.getProperty("redis.host", "127.0.0.1:8379");
        String user = System.getProperty("redis.user");
        String password = System.getProperty("redis.password");
        Assume.assumeTrue("Provide -Dredis.user and -Dredis.password",
                user != null && password != null);
        ShellConnect connect = new ShellConnect();
        connect.setName("redis-v2-live");
        connect.setHost(host);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setConnectTimeOut(3);
        connect.setExecuteTimeOut(3);
        connect.setReadonly(false);

        ShellRedisClient client = new ShellRedisClient(connect);
        String suffix = UUID.randomUUID().toString();
        String key = "easyshell:redis-v2:regression:" + suffix;
        String transactionKey = key + ":tx";
        try {
            client.startDatabase(0);
            RedisCommandRegistry registry = RedisCommandRegistry.load(client);
            assertNotNull(registry.find(List.of("get")));
            assertNotNull(registry.find(List.of("set")));
            assertNotNull(registry.find(List.of("command", "docs")));

            try (ShellRedisCommandSession session = client.openCommandSession(0)) {
                RedisCommandExecutor executor = new RedisCommandExecutor();
                assertTrue(response(executor, List.of("ping"), registry, session).contains("PONG"));
                assertTrue(response(executor, List.of("set", key, "value-" + suffix), registry, session).contains("OK"));
                assertTrue(response(executor, List.of("get", key), registry, session).contains("value-" + suffix));

                assertTrue(response(executor, List.of("multi"), registry, session).contains("OK"));
                assertTrue(response(executor, List.of("set", transactionKey, "tx"), registry, session).contains("QUEUED"));
                assertTrue(response(executor, List.of("exec"), registry, session).contains("OK"));
                assertTrue(response(executor, List.of("get", transactionKey), registry, session).contains("tx"));

                assertTrue(response(executor, List.of("select", "1"), registry, session).contains("OK"));
                assertTrue(response(executor, List.of("set", key + ":db1", "db1"), registry, session).contains("OK"));
                assertTrue(response(executor, List.of("get", key + ":db1"), registry, session).contains("db1"));
                assertTrue(response(executor, List.of("select", "0"), registry, session).contains("OK"));

                assertTrue(response(executor, List.of("acl", "whoami"), registry, session).contains("admin"));
                assertTrue(response(executor, List.of("command", "docs", "get"), registry, session).contains("get"));
                assertTrue(session.commandGetKeys(List.of("mget", key, transactionKey)).contains(key));
            }
        } finally {
            try (ShellRedisCommandSession cleanup = client.openCommandSession(0)) {
                cleanup.execute(RedisCommandBuilder.build(
                        List.of("unlink", key, transactionKey, key + ":db1"), null,
                        List.of(key, transactionKey, key + ":db1")));
            } catch (Exception ignored) {
            }
            client.close();
        }
    }

    private String response(RedisCommandExecutor executor, List<String> command,
                            RedisCommandRegistry registry, ShellRedisCommandSession session) throws Exception {
        return RedisResponseFormatter.format(
                executor.execute(command, registry.find(command), session), "\n");
    }
}
