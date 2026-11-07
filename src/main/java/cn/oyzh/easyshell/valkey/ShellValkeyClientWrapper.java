//package cn.oyzh.easyshell.valkey;
//
//import io.valkey.BuilderFactory;
//import io.valkey.Connection;
//import io.valkey.Protocol;
//import io.valkey.ValkeyClient;
//import io.valkey.resps.Slowlog;
//
//import java.util.List;
//import java.util.Map;
//
//import static io.valkey.Protocol.Command.CLIENT;
//import static io.valkey.Protocol.Command.MOVE;
//import static io.valkey.Protocol.Command.PUBSUB;
//import static io.valkey.Protocol.Command.ROLE;
//import static io.valkey.Protocol.Command.SLOWLOG;
//import static io.valkey.Protocol.Keyword.CHANNELS;
//import static io.valkey.Protocol.Keyword.LIST;
//import static io.valkey.Protocol.toByteArray;
//import static io.valkey.util.SafeEncoder.encode;
//
///**
// *
// * @author oyzh
// * @since 2026-07-01
// */
//public class ShellValkeyClientWrapper {
//
//    private ValkeyClient redisClient;
//
//    public ShellValkeyClientWrapper(ValkeyClient redisClient) {
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
