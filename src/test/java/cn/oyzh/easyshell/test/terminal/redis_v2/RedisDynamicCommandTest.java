package cn.oyzh.easyshell.test.terminal.redis_v2;

import cn.oyzh.easyshell.terminal.redis2.RedisCommandBuilder;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandDescriptor;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandRegistry;
import cn.oyzh.easyshell.terminal.redis2.RedisHelpCommandHandler;
import cn.oyzh.easyshell.terminal.redis2.RedisResponseFormatter;
import org.junit.Test;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.resps.CommandInfo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisDynamicCommandTest {

    @Test
    public void discoversCommandMetadataWithoutCommandClasses() {
        CommandInfo getInfo = new CommandInfo(2, List.of("readonly"), 1, 1, 1, List.of(), List.of(), List.of());
        RedisCommandRegistry registry = RedisCommandRegistry.from(Map.of("get", getInfo));

        RedisCommandDescriptor descriptor = registry.find(List.of("GET"));
        assertEquals("get", descriptor.displayName());
        assertTrue(descriptor.flags().contains("readonly"));
        assertEquals(List.of("get"), registry.commandNames());
    }

    @Test
    public void buildsRawCommandAndMarksKeysForClusterRouting() {
        RedisCommandDescriptor descriptor = new RedisCommandDescriptor(
                "mget", List.of("mget"), -2, List.of("readonly"), 1, -1, 1, null, Map.of());
        CommandObject<Object> command = RedisCommandBuilder.build(
                List.of("mget", "key-a", "key-b"), descriptor, List.of("key-a", "key-b"));

        assertEquals(List.of("key-a", "key-b"), command.getArguments().getKeys());
    }

    @Test
    public void formatsRedisCliTypes() {
        String output = RedisResponseFormatter.format(List.of(
                "OK",
                "hello".getBytes(StandardCharsets.UTF_8),
                2L,
                List.of("one".getBytes(StandardCharsets.UTF_8))
        ), "\n");

        assertEquals("1) OK\n"
                + "2) \"hello\"\n"
                + "3) (integer) 2\n"
                + "4) 1) \"one\"", output);
        assertEquals("(nil)", RedisResponseFormatter.format(null, "\n"));
    }

    @Test
    public void rendersDynamicHelp() {
        CommandInfo setInfo = new CommandInfo(3, List.of("write", "denyoom"), 1, 1, 1, List.of(), List.of(), List.of());
        RedisCommandRegistry registry = RedisCommandRegistry.from(Map.of("set", setInfo));

        String help = RedisHelpCommandHandler.render("help set", registry, "\n");
        assertTrue(help.contains("set [arguments]"));
        assertTrue(help.contains("flags: write, denyoom"));
    }
}
