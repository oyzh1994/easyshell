package cn.oyzh.easyshell.redis;

import redis.clients.jedis.CommandObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.args.Rawable;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 命令会话。非集群模式下复用同一个连接，保证 MULTI、WATCH、SELECT 等连接状态命令的语义。
 */
public final class ShellRedisCommandSession implements AutoCloseable {

    private final ShellRedisClient client;
    private final Jedis jedis;
    private final JedisCluster cluster;
    private final int initialDb;
    private boolean closed;
    private boolean transaction;
    private boolean watching;
    private int currentDb;

    ShellRedisCommandSession(ShellRedisClient client, Jedis jedis, JedisCluster cluster) {
        this.client = client;
        this.jedis = jedis;
        this.cluster = cluster;
        this.initialDb = jedis == null ? 0 : jedis.getDB();
        this.currentDb = this.initialDb;
    }

    public <T> T execute(CommandObject<T> command) {
        this.checkOpen();
        List<Rawable> arguments = new ArrayList<>();
        command.getArguments().forEach(arguments::add);
        String commandName = arguments.isEmpty() ? "" : text(arguments.getFirst());
        boolean startsTransaction = "multi".equalsIgnoreCase(commandName);
        boolean endsTransaction = "exec".equalsIgnoreCase(commandName) || "discard".equalsIgnoreCase(commandName);
        boolean startsWatch = "watch".equalsIgnoreCase(commandName);
        boolean endsWatch = "unwatch".equalsIgnoreCase(commandName) || endsTransaction;
        try {
            T response = this.cluster == null
                    ? this.jedis.getConnection().executeCommand(command)
                    : this.cluster.executeCommand(command);
            if (startsTransaction) {
                this.transaction = true;
            } else if (endsTransaction) {
                this.transaction = false;
            }
            if (startsWatch) {
                this.watching = true;
            } else if (endsWatch) {
                this.watching = false;
            }
            if (this.jedis != null && "select".equalsIgnoreCase(commandName) && arguments.size() > 1) {
                this.currentDb = Integer.parseInt(text(arguments.get(1)));
            }
            return response;
        } catch (RuntimeException ex) {
            if (startsTransaction) {
                this.transaction = false;
            }
            throw ex;
        }
    }

    public boolean isTransaction() {
        return this.transaction;
    }

    public List<String> commandGetKeys(List<String> command) {
        ProtocolCommand commandType = () -> SafeEncoder.encode("COMMAND");
        CommandArguments arguments = new CommandArguments(commandType);
        arguments.add("GETKEYS");
        command.forEach(arguments::add);
        List<Object> response = this.execute(new CommandObject<>(arguments, BuilderFactory.ENCODED_OBJECT_LIST));
        return response.stream().map(String::valueOf).toList();
    }

    public boolean isCluster() {
        return this.cluster != null;
    }

    private static String text(Rawable argument) {
        return new String(argument.getRaw(), StandardCharsets.UTF_8);
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Redis command session is closed");
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        try {
            if (this.transaction) {
                this.control("DISCARD");
            }
            if (this.watching) {
                this.control("UNWATCH");
            }
            if (this.jedis != null && this.currentDb != this.initialDb) {
                this.jedis.select(this.initialDb);
                this.currentDb = this.initialDb;
            }
        } finally {
            this.closed = true;
            this.client.releaseCommandResource(this.jedis);
        }
    }

    private void control(String commandName) {
        ProtocolCommand command = () -> SafeEncoder.encode(commandName);
        this.execute(new CommandObject<>(new CommandArguments(command), BuilderFactory.RAW_OBJECT));
    }
}
