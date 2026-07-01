//package cn.oyzh.easyshell.redis;
//
//import redis.clients.jedis.BuilderFactory;
//import redis.clients.jedis.Connection;
//import redis.clients.jedis.Protocol;
//import redis.clients.jedis.RedisClient;
//import redis.clients.jedis.resps.Slowlog;
//
//import java.util.List;
//import java.util.Map;
//
//import static redis.clients.jedis.Protocol.Command.CLIENT;
//import static redis.clients.jedis.Protocol.Command.MOVE;
//import static redis.clients.jedis.Protocol.Command.PUBSUB;
//import static redis.clients.jedis.Protocol.Command.ROLE;
//import static redis.clients.jedis.Protocol.Command.SLOWLOG;
//import static redis.clients.jedis.Protocol.Keyword.CHANNELS;
//import static redis.clients.jedis.Protocol.Keyword.LIST;
//import static redis.clients.jedis.Protocol.toByteArray;
//import static redis.clients.jedis.util.SafeEncoder.encode;
//
///**
// *
// * @author oyzh
// * @since 2026-07-01
// */
//public class ShellRedisClientWrapper {
//
//    private RedisClient redisClient;
//
//    public ShellRedisClientWrapper(RedisClient redisClient) {
//        this.redisClient = redisClient;
//    }
//
//    private Connection connection() {
//        return redisClient.getPool().getResource();
//    }
//
//    public Map<String, String> configGet(final String pattern) {
//        Connection connection = this.connection();
//        connection.sendCommand(Protocol.Command.CONFIG, Protocol.Keyword.GET.name(), pattern);
//        return BuilderFactory.STRING_MAP.build(connection.getOne());
//    }
//
//    public List<Object> role() {
//        Connection connection = this.connection();
//        connection.sendCommand(ROLE);
//        return BuilderFactory.ENCODED_OBJECT_LIST.build(connection.getOne());
//    }
//
//    public List<String> pubsubChannels() {
//        Connection connection = this.connection();
//        connection.sendCommand(PUBSUB, CHANNELS);
//        return connection.getMultiBulkReply();
//    }
//
//    public List<String> pubsubChannels(final String pattern) {
//        Connection connection = this.connection();
//        connection.sendCommand(PUBSUB, CHANNELS.name(), pattern);
//        return connection.getMultiBulkReply();
//    }
//
//    public List<Slowlog> slowlogGet() {
//        Connection connection = this.connection();
//        connection.sendCommand(SLOWLOG, Protocol.Keyword.GET);
//        return Slowlog.from(connection.getObjectMultiBulkReply());
//    }
//
//    public List<Slowlog> slowlogGet(final long entries) {
//        Connection connection = this.connection();
//        connection.sendCommand(SLOWLOG, Protocol.Keyword.GET.getRaw(), toByteArray(entries));
//        return Slowlog.from(connection.getObjectMultiBulkReply());
//    }
//
//    public String clientList() {
//        Connection connection = this.connection();
//        connection.sendCommand(CLIENT, LIST);
//        return connection.getBulkReply();
//    }
//
//    public long move(final String key, final int dbIndex) {
//        Connection connection = this.connection();
//        connection.sendCommand(MOVE, encode(key), toByteArray(dbIndex));
//        return connection.getIntegerReply();
//    }
//}
