package cn.oyzh.easyshell.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.dto.redis.RedisInfoProp;
import cn.oyzh.easyshell.exception.redis.ClusterOperationException;
import cn.oyzh.easyshell.exception.ReadonlyOperationException;
import cn.oyzh.easyshell.exception.redis.SentinelOperationException;
import cn.oyzh.easyshell.exception.redis.UnsupportedCommandException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalUtil;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.jump.SSHJumpForwarder2;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.CommandObjects;
import redis.clients.jedis.Connection;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.args.BitCountOption;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.BitPosParams;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.resps.CommandInfo;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Slowlog;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamFullInfo;
import redis.clients.jedis.resps.StreamInfo;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;
import redis.clients.jedis.util.Pool;

import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * redis终端
 *
 * @author oyzh
 * @since 2023/6/16
 */
public class ShellRedisClient implements ShellBaseClient {

//    /**
//     * redis连接池
//     */
//    private JedisPool pool;

    // /**
    //  * 静默关闭标志位
    //  */
    // private boolean closeQuietly;

    /**
     * 连接池管理器
     */
    private final ShellRedisPoolManager poolManager = new ShellRedisPoolManager();

    // /**
    //  * redis集群操作对象
    //  */
    // private JedisCluster cluster;

    /**
     * redis命令对象
     */
    private CommandObjects commandObjects;

//    /**
//     * redis哨兵连接池
//     */
//    private JedisSentinelPool sentinelPool;

    /**
     * 当前连接角色
     */
    private String role;

    public String getRole() {
        return role;
    }

    /**
     * 当前db索引
     */
    private int dbIndex = 0;

    /**
     * 数据库数量
     */
    private Integer databases;

    /**
     * 服务属性
     */
    private RedisInfoProp infoProp;

    /**
     * ssh端口转发器
     */
    private SSHJumpForwarder2 jumpForwarder;

    /**
     * redis信息
     */
    private final ShellConnect shellConnect;

    public ShellConnect shellConnect() {
        return shellConnect;
    }

    // /**
    //  * cluster集群的主节点连接
    //  */
    // private List<ConnectionPool> clusterMasterPools;

    // /**
    //  * 连接状态
    //  */
    // private final ReadOnlyObjectWrapper<RedisConnState> state = new ReadOnlyObjectWrapper<>(RedisConnState.NOT_INITIALIZED);

//    /**
//     * ssh配置储存
//     */
//    private final RedisSSHConfigStore sshConfigStore = RedisSSHConfigStore.INSTANCE;

    // /**
    //  * 跳板配置存储
    //  */
    // private final RedisJumpConfigStore jumpConfigStore = RedisJumpConfigStore.INSTANCE;

    // /**
    //  * 获取连接状态
    //  *
    //  * @return 连接状态
    //  */
    // public RedisConnState state() {
    //     return this.stateProperty().get();
    // }

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    public ShellRedisClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        // if (redisConnect.isSSHForward() && redisConnect.getSshConfig() != null) {
        //     this.sshForwarder = new SSHForwarder(redisConnect.getSshConfig());
        // }
        // this.stateProperty().addListener((observable, oldValue, newValue) -> {
        //     switch (newValue) {
        //         case CLOSED -> {
        //             if (!this.closeQuietly) {
        //                 RedisEventUtil.connectionClosed(this);
        //             }
        //         }
        //         case CONNECTED -> RedisEventUtil.connectionConnected(this);
        //         default -> {
        //
        //         }
        //     }
        // });
        this.addStateListener(this.stateListener);
    }

    // /**
    //  * 连接状态属性
    //  *
    //  * @return 连接状态属性
    //  */
    // public ReadOnlyObjectProperty<RedisConnState> stateProperty() {
    //     return this.state.getReadOnlyProperty();
    // }

//     /**
//      * 添加连接状态监听器
//      *
//      * @param stateListener 监听器
//      */
//     public void addStateListener(ChangeListener<RedisConnState> stateListener) {
//         if (stateListener != null) {
//             this.state.addListener(stateListener);
// //            this.state.addListener(new WeakChangeListener<>(stateListener));
//         }
//     }

    /**
     * 初始化连接
     *
     * @return 连接
     */
    private String initHost() {
        // 连接地址
        String host;
//        // 初始化跳板配置
//        List<RedisJumpConfig> jumpConfigs = this.redisConnect.getJumpConfigs();
//        // 从数据库获取
//        if (jumpConfigs == null) {
//            jumpConfigs = this.jumpConfigStore.loadByIid(this.redisConnect.getId());
//        }
//        // 过滤配置
//        jumpConfigs = jumpConfigs == null ? Collections.emptyList() : jumpConfigs.stream().filter(RedisJumpConfig::isEnabled).collect(Collectors.toList());
        // 初始化跳板转发
        if (this.shellConnect.isEnableJump()) {
            if (this.jumpForwarder == null) {
                this.jumpForwarder = new SSHJumpForwarder2();
            }
            // 初始化跳板配置
            List<ShellJumpConfig> jumpConfigs = this.shellConnect.getJumpConfigs();
            // 转换为目标连接
            SSHConnect target = new SSHConnect();
            target.setHost(this.shellConnect.hostIp());
            target.setPort(this.shellConnect.hostPort());
            // 执行连接
            int localPort = this.jumpForwarder.forward(jumpConfigs, target);
            // 连接信息
            host = "127.0.0.1:" + localPort;
        } else {// 直连
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
                this.jumpForwarder = null;
            }
            // 连接信息
            host = this.shellConnect.hostIp() + ":" + this.shellConnect.hostPort();
        }
        return host;
    }

    /**
     * 初始化客户端
     */
    private void initClient(int connectTimeout) throws Exception {
        String hostAddr = this.initHost();
        String hostIp = hostAddr.split(":")[0];
        int port = Integer.parseInt(hostAddr.split(":")[1]);
        // HostAndPort host = new HostAndPort(hostIp, port);
//        // ssh端口转发
//        if (this.redisConnect.isSSHForward()) {
//            // 初始化ssh转发器
//            RedisSSHConfig sshConfig = this.redisConnect.getSshConfig();
//            // 从数据库获取
//            if (sshConfig == null) {
//                sshConfig = this.sshConfigStore.getByIid(this.redisConnect.getId());
//            }
//            if (sshConfig != null) {
//                if (this.sshJumper == null) {
//                    this.sshJumper = new SSHJumpForwarder();
//                }
//                // ssh配置
//                // 执行连接
//                int localPort = this.sshJumper.forward(null, null);
//                // 连接信息
//                host = new HostAndPort("127.0.0.1", localPort);
//            } else {
//                JulLog.warn("ssh forward is enable but ssh config is null");
//                throw new RedisException("ssh forward is enable but ssh config is null");
//            }
//            // SSHForwardConfig forwardInfo = new SSHForwardConfig();
//            // forwardInfo.setHost(this.redisConnect.hostIp());
//            // forwardInfo.setPort(this.redisConnect.hostPort());
//            // int localPort = this.sshForwarder.forward(forwardInfo);
//            // // 连接信息
//            // host = new HostAndPort("127.0.0.1", localPort);
//        } else {// 直连
//            // 连接信息
//            host = new HostAndPort(this.redisConnect.hostIp(), this.redisConnect.hostPort());
//        }
        // 客户端配置
        DefaultJedisClientConfig clientConfig = ShellRedisClientUtil.newConfig(this.shellConnect);
        // 初始化连接池
        this.initPool(hostIp, port, clientConfig);
        try {
            // 获取当前角色
            this.role = (String) CollectionUtil.getFirst(this.role());
        } catch (UnsupportedCommandException | JedisDataException ignored) {
        }
        // cluster集群模式
        if (this.isClusterMode()) {
            // 初始化cluster集群
            this.initCluster(hostIp, port, clientConfig);
        }
    }

    /**
     * 初始化cluster集群
     *
     * @param host         地址
     * @param port         端口
     * @param clientConfig 客户端配置
     */
    private void initCluster(String host, int port, DefaultJedisClientConfig clientConfig) {
        // 集群连接池配置
        ConnectionPoolConfig clusterPoolConfig = new ConnectionPoolConfig();
        // 初始化连接池
        this.intPoolConfig(clusterPoolConfig);
        // 初始化cluster集群操作对象
        JedisCluster cluster = new JedisCluster(new HostAndPort(host, port), clientConfig, 10, clusterPoolConfig);
        // 集群配置
        this.poolManager.setCluster(cluster);
        // 初始化指令对象
        this.commandObjects = new CommandObjects();
    }

    /**
     * 初始化连接池
     *
     * @param host         地址
     * @param port         端口
     * @param clientConfig 客户端配置
     */
    private void initPool(String host, int port, DefaultJedisClientConfig clientConfig) {
        // 连接池配置
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 初始化连接池
        this.intPoolConfig(poolConfig);
        // 连接池
        JedisPool pool;
        // s大力
        Proxy proxy = null;
        // ssl工厂
        SSLSocketFactory sslSocketFactory = null;
        // 开启ssl
        if (this.shellConnect.isSSLMode()) {
            sslSocketFactory = clientConfig.getSslSocketFactory();
        }
        // 开启代理
        if (this.shellConnect.isEnableProxy()) {
            proxy = ShellProxyUtil.initProxy1(this.shellConnect.getProxyConfig());
        }
        // 生成socket工厂
        ShellRedisSocketFactory socketFactory = new ShellRedisSocketFactory(sslSocketFactory, host, port, proxy, clientConfig.getSocketTimeoutMillis());
        // 生成连接池
        pool = new JedisPool(poolConfig, socketFactory, clientConfig);
        // 连接配置
        this.poolManager.setJedisPool(pool);
        this.poolManager.initResource();
        this.poolManager.setConnectName(this.connectName());
    }

    /**
     * 初始化连接池配置
     *
     * @param poolConfig 连接池配置
     */
    private void intPoolConfig(GenericObjectPoolConfig<?> poolConfig) {
        // 最小空闲
        poolConfig.setMinIdle(3);
        // 最大空闲
        poolConfig.setMaxIdle(16);
        // 最大连接
        poolConfig.setMaxTotal(50);
        // 创建时测试
        poolConfig.setTestOnCreate(true);
        // 获取时测试
        poolConfig.setTestOnBorrow(true);
        // 空闲时测试
        poolConfig.setTestWhileIdle(true);
        // 归还时测试
        poolConfig.setTestOnReturn(true);
        // 最大超时
        poolConfig.setMaxWait(Duration.ofSeconds(10));
    }

    /**
     * 是否cluster集群模式
     *
     * @return 结果
     */
    public boolean isClusterMode() {
        return Objects.equals(1, this.infoProp().getClusterEnabled());
    }

    /**
     * 如果集群连接不支持操作，则抛出异常
     */
    public void throwClusterException() {
        if (this.isClusterMode()) {
            throw new ClusterOperationException();
        }
    }

    /**
     * 是否master集群模式
     *
     * @return 结果
     */
    public boolean isMasterMode() {
        return !this.isClusterMode();
    }

    /**
     * 是否哨兵模式
     *
     * @return 结果
     */
    public boolean isSentinelMode() {
        return StringUtil.equalsIgnoreCase("sentinel", this.role);
    }

    /**
     * 如果哨兵连接不支持操作，则抛出异常
     */
    public void throwSentinelException() {
        if (this.isSentinelMode()) {
            throw new SentinelOperationException();
        }
    }

    /**
     * 如果只读模式不支持操作，则抛出异常
     */
    public void throwReadonlyException() {
        if (this.isReadonly()) {
            throw new ReadonlyOperationException();
        }
    }

    /**
     * 如果指令不支持操作，则抛出异常
     *
     * @param command 指令
     */
    public void throwCommandException(String command) {
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), command);
    }

    /**
     * 是否单机模式
     *
     * @return 结果
     */
    public boolean isStandaloneMode() {
        return StringUtil.equalsIgnoreCase("standalone", this.infoProp().getRedisMode());
    }

    /**
     * 是否只读模式
     *
     * @return 结果
     */
    public boolean isReadonly() {
        return this.shellConnect.isReadonly();
    }

    /**
     * 获取集群操作对象
     *
     * @return JedisCluster
     */
    private JedisCluster getCluster() {
        return this.poolManager.getCluster();
        // return this.cluster;
    }

// /**
//  * 获取cluster集群的主节点连接
//  *
//  * @return List<ConnectionPool>
//  */
// @Deprecated
// private List<ConnectionPool> getClusterMasterPools() {
//     Map<String, ConnectionPool> poolMap = this.cluster.getClusterNodes();
//     if (CollectionUtil.isNotEmpty(poolMap) && (this.clusterMasterPools == null || !poolMap.values().containsAll(this.clusterMasterPools))) {
//         this.clusterMasterPools = new ArrayList<>();
//         for (ConnectionPool connectionPool : poolMap.values()) {
//             List<Object> roleList = this.role(connectionPool);
//             Object role = CollectionUtil.getFirst(roleList);
//             if (StringUtil.equalsIgnoreCase("master", (String) role)) {
//                 this.clusterMasterPools.add(connectionPool);
//             }
//         }
//     }
//     return this.clusterMasterPools == null ? Collections.emptyList() : this.clusterMasterPools;
// }

    /**
     * 获取cluster集群的主节点连接
     *
     * @return List<ConnectionPool>
     */
    private List<ConnectionPool> getClusterPools() {
        if (!this.poolManager.hasClusterPool()) {
            Map<String, ConnectionPool> poolMap = this.getCluster().getClusterNodes();
            this.poolManager.initClusterPool(poolMap);
        }
        return this.poolManager.getClusterPools();
    }

    /**
     * 获取连接
     *
     * @return Jedis
     */
    private Jedis getResource() {
        return this.poolManager.getResource(null);
    }

    /**
     * 获取连接
     *
     * @param dbIndex 数据库索引
     * @return Jedis
     */
    private Jedis getResource(Integer dbIndex) {
        return this.poolManager.getResource(dbIndex);
    }

//    /**
//     * 获取连接
//     *
//     * @return Jedis
//     */
//    private Jedis getResource() {
//        try {
//            if (this.sentinelPool != null) {
//                return this.sentinelPool.getResource();
//            }
//            if (this.pool != null) {
//                return this.pool.getResource();
//            }
//        } finally {
//            ThreadLocalUtil.setVal("connectName", this.connectName());
//        }
//        return null;
//    }

    /**
     * 返还连接
     *
     * @param jedis 连接
     */
    private void returnResource(Jedis jedis) {
//        ThreadUtil.startVirtual(() -> {
//            if (this.sentinelPool != null) {
//                this.sentinelPool.returnResource(jedis);
//            } else if (this.pool != null) {
//                this.pool.returnResource(jedis);
//            }
//        });
        this.poolManager.returnResource(jedis);
    }

    /**
     * 关闭客户端，静默模式
     */
    public void closeQuiet() {
        // this.closeQuietly = true;
        this.close();
    }

    @Override
    public void close() {
        try {
            // boolean isClosed = false;
            // // 关闭集群
            // if (this.cluster != null) {
            //     this.cluster.close();
            //     isClosed = true;
            // }
//            // 关闭连接池
//            if (this.pool != null && !this.pool.isClosed()) {
//                this.pool.close();
//                isClosed = true;
//            }
//            // 关闭哨兵连接池
//            if (this.sentinelPool != null && !this.sentinelPool.isClosed()) {
//                this.sentinelPool.close();
//                isClosed = true;
//            }
            // 销毁端口转发
            if (this.jumpForwarder != null) {
                IOUtil.close(this.jumpForwarder);
            }
            this.poolManager.destroy();
            // 已关闭
            // if (isClosed) {
            this.state.set(ShellConnState.CLOSED);
//                RedisEventUtil.connectionClosed(this);
            // }
            // 重置变量
//            this.pool = null;
            this.role = null;
            // this.cluster = null;
            this.databases = null;
            this.clearInfoProp();
//            this.sentinelPool = null;
            this.commandObjects = null;
            this.removeStateListener(this.stateListener);
            // this.clusterMasterPools = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 重置客户端
     */
    public void reset() {
        // 移除监听器
//        this.close();
        this.state.set(ShellConnState.NOT_INITIALIZED);
    }

    // /**
    //  * 开始连接客户端
    //  */
    // public void start() {
    //     this.startDatabase(0, this.shellConnect.connectTimeOutMs());
    // }

    @Override
    public void start(int connectTimeout) {
        this.startDatabase(0, connectTimeout);
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 开始连接客户端
     *
     * @param dbIndex 默认db索引
     */
    public void startDatabase(int dbIndex) {
        this.startDatabase(dbIndex, this.shellConnect.connectTimeOutMs());
    }

    /**
     * 错误信息
     */
    private String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 开始连接客户端
     *
     * @param dbIndex        默认db索引
     * @param connectTimeout 连接超时
     */
    public void startDatabase(int dbIndex, int connectTimeout) {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            this.errorMsg = null;
            // 初始化连接池
            this.state.set(ShellConnState.CONNECTING);
            // 初始化客户端
            this.initClient(connectTimeout);
            // 初始化数据库
            if (!this.isClusterMode() && !this.isSentinelMode()) {
                this.select(dbIndex);
            }
            this.state.set(ShellConnState.CONNECTED);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("redisClient start error", ex);
            this.errorMsg = ex.getMessage();
//            throw new RedisException(ex);
        }
    }

    /**
     * 连接是否失效
     *
     * @return 结果
     */
    public boolean isBroken() {
        Jedis jedis = this.getResource();
        try {
            return jedis.isBroken();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        if (!this.isClosed()) {
            return this.state.get() == ShellConnState.CONNECTING;
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        Pool<?> pool = this.getPool();
        return pool != null && !pool.isClosed();
    }

    // /**
    //  * 是否已关闭
    //  *
    //  * @return 结果
    //  */
    // public boolean isClosed() {
    //     if (this.getPool() == null || this.getPool().isClosed()) {
    //         return true;
    //     }
    //     return !this.state.get().isConnected();
    // }

    /**
     * 获取连接池
     *
     * @return 连接池
     */
    private Pool<?> getPool() {
        return this.poolManager.getJedisPool();
//        return this.pool == null ? this.sentinelPool : this.pool;
    }

    /**
     * 检测连接
     *
     * @return 结果
     */
    public String ping() {
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "ping");
        if (this.isClusterMode()) {
            return this.getCluster().ping();
        }
        Jedis jedis = this.getResource();
        try {
            return jedis.ping();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 打印内容
     *
     * @param string 内容
     * @return 结果
     */
    public String echo(String string) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "echo");
        Jedis jedis = this.getResource();
        try {
            return jedis.echo(string);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 返回随机hash字段
     *
     * @param dbIndex db索引
     * @param key     键
     * @return hash字段
     */
    public String hrandfield(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hrandfield");
        if (this.isClusterMode()) {
            return this.getCluster().hrandfield(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hrandfield(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 返回随机hash字段
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   数量
     * @return hash字段
     */
    public List<String> hrandfield(Integer dbIndex, String key, long count) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hrandfield");
        if (this.isClusterMode()) {
            return this.getCluster().hrandfield(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hrandfield(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 返回随机hash字段及值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   数量
     * @return hash字段及值
     */
    public List<Map.Entry<String, String>> hrandfieldWithValues(Integer dbIndex, String key, long count) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hrandfield");
        if (this.isClusterMode()) {
            return this.getCluster().hrandfieldWithValues(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hrandfieldWithValues(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash字段列表
     *
     * @param dbIndex db索引
     * @param key     键
     * @return hash字段列表
     */
    public Set<String> hkeys(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hkeys");
        if (this.isClusterMode()) {
            return this.getCluster().hkeys(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hkeys(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash值列表
     *
     * @param dbIndex db索引
     * @param key     键
     * @return hash值列表
     */
    public List<String> hvals(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hvals");
        if (this.isClusterMode()) {
            return this.getCluster().hvals(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hvals(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash数据个数
     *
     * @param dbIndex db索引
     * @param key     键
     * @return hash数据个数
     */
    public long hlen(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hlen");
        if (this.isClusterMode()) {
            return this.getCluster().hlen(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hlen(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除hash字段
     *
     * @param dbIndex db索引
     * @param key     键
     * @param fields  字段
     * @return 受影响的数据数量
     */
    public long hdel(Integer dbIndex, String key, String... fields) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hdel");
        if (this.isClusterMode()) {
            return this.getCluster().hdel(key);
        }
        if (ArrayUtil.isNotEmpty(fields)) {
            Jedis jedis = this.getResource(dbIndex);
            try {
                this.dbIndex(jedis, dbIndex);
                return jedis.hdel(key, fields);
            } finally {
                this.returnResource(jedis);
            }
        }
        return -1L;
    }

    /**
     * 设置hash内容
     *
     * @param dbIndex db索引
     * @param key     键
     * @param field   字段
     * @param value   值
     * @return 受影响的数据数量
     */
    public long hset(Integer dbIndex, String key, String field, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hset");
        if (this.isClusterMode()) {
            return this.getCluster().hset(key, field, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hset(key, field, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置hash内容
     *
     * @param dbIndex db索引
     * @param key     键
     * @param hash    字段及
     * @return 受影响的数据数量
     */
    public long hset(Integer dbIndex, String key, Map<String, String> hash) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hset");
        if (this.isClusterMode()) {
            return this.getCluster().hset(key, hash);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hset(key, hash);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置hash内容，hash键需存在
     *
     * @param dbIndex db索引
     * @param key     键
     * @param field   字段
     * @param value   值
     * @return 受影响的数据数量
     */
    public long hsetnx(Integer dbIndex, String key, String field, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hsetnx");
        if (this.isClusterMode()) {
            return this.getCluster().hsetnx(key, field, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hsetnx(key, field, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash字段值长度
     *
     * @param dbIndex db索引
     * @param key     键
     * @param field   字段
     * @return 字段值长度
     */
    public long hstrlen(Integer dbIndex, String key, String field) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hstrlen");
        if (this.isClusterMode()) {
            return this.getCluster().hstrlen(key, field);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hstrlen(key, field);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置hash多个字段
     *
     * @param dbIndex db索引
     * @param key     键
     * @param hash    字段及值
     * @return 结果
     */
    public String hmset(Integer dbIndex, String key, Map<String, String> hash) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hmset");
        if (this.isClusterMode()) {
            return this.getCluster().hmset(key, hash);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hmset(key, hash);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 判断hash字段是否存在
     *
     * @param dbIndex db索引
     * @param key     键
     * @param field   字段
     * @return 结果
     */
    public boolean hexists(Integer dbIndex, String key, String field) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hexists");
        if (this.isClusterMode()) {
            return this.getCluster().hexists(key, field);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hexists(key, field);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param field   字段
     * @return 值
     */
    public String hget(Integer dbIndex, String key, String field) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hget");
        if (this.isClusterMode()) {
            return this.getCluster().hget(key, field);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hget(key, field);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加hash字段值
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param field     字段
     * @param increment 增加值
     * @return 新值
     */
    public long hincrBy(Integer dbIndex, String key, String field, long increment) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hincrBy");
        if (this.isClusterMode()) {
            return this.getCluster().hincrBy(key, field, increment);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hincrBy(key, field, increment);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加hash字段值，以浮点形式
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param field     字段
     * @param increment 增加值
     * @return 新值
     */
    public double hincrByFloat(Integer dbIndex, String key, String field, double increment) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hincrByFloat");
        if (this.isClusterMode()) {
            return this.getCluster().hincrByFloat(key, field, increment);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hincrByFloat(key, field, increment);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash多个值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param fields  字段
     * @return 值
     */
    public List<String> hmget(Integer dbIndex, String key, String... fields) {
        if (ArrayUtil.isEmpty(fields)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hmget");
        if (this.isClusterMode()) {
            return this.getCluster().hmget(key, fields);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hmget(key, fields);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hash所有字段及值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 所有字段及值
     */
    public Map<String, String> hgetAll(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "hgetAll");
        if (this.isClusterMode()) {
            return this.getCluster().hgetAll(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.hgetAll(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset成员数量
     *
     * @param dbIndex db索引
     * @param key     键
     * @return zset成员数量
     */
    public long zcard(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zcard");
        if (this.isClusterMode()) {
            return this.getCluster().zcard(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zcard(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset指定分数区间成员数量
     *
     * @param dbIndex db索引
     * @param key     键
     * @param min     最小分数
     * @param max     最大分数
     * @return 指定分数区间成员数量
     */
    public long zcount(Integer dbIndex, String key, double min, double max) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zcount");
        if (this.isClusterMode()) {
            return this.getCluster().zcount(key, min, max);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zcount(key, min, max);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个zset的差集
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return zset差集
     */
    public List<String> zdiff(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zdiff");
        if (this.isClusterMode()) {
            return this.getCluster().zdiff(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zdiff(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个zset的差集，并返回分数
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return zset差集及分数
     */
    public List<Tuple> zdiffWithScores(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zdiff");
        if (this.isClusterMode()) {
            return this.getCluster().zdiffWithScores(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zdiffWithScores(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个zset的差集，并保存到目标zset
     *
     * @param dbIndex db索引
     * @param destkey 目标zset
     * @param keys    键
     * @return 受影响的数据数量
     */
    public long zdiffStore(Integer dbIndex, String destkey, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zdiffStore");
        if (this.isClusterMode()) {
            return this.getCluster().zdiffStore(destkey, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zdiffStore(destkey, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset成员分数
     *
     * @param dbIndex db索引
     * @param key     键
     * @param member  成员
     * @return 分数
     */
    public Double zscore(Integer dbIndex, String key, String member) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zscore");
        if (this.isClusterMode()) {
            return this.getCluster().zscore(key, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zscore(key, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset多个成员分数
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 分数
     */
    public List<Double> zmscore(Integer dbIndex, String key, String... members) {
        if (ArrayUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zmscore");
        if (this.isClusterMode()) {
            return this.getCluster().zmscore(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zmscore(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset多个成员分数，扩展功能，兼容老版本
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 分数
     */
    public List<Double> zmscore_ext(Integer dbIndex, String key, String... members) {
        if (ShellRedisVersionUtil.isSupported(this.getServerVersion(), ShellRedisVersionUtil.getSupportedVersion("zmscore"))) {
            return this.zmscore(dbIndex, key, members);
        }
        if (ArrayUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        if (this.isClusterMode()) {
            List<Double> list = new ArrayList<>(members.length);
            for (String member : members) {
                list.add(this.getCluster().zscore(key, member));
            }
            return list;
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            List<Double> list = new ArrayList<>(members.length);
            for (String member : members) {
                list.add(jedis.zscore(key, member));
            }
            return list;
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 随机返回zset成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @return zset成员
     */
    public String zrandmember(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrandmember");
        if (this.isClusterMode()) {
            return this.getCluster().zrandmember(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrandmember(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 随机返回zset成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   返回数量
     * @return zset成员
     */
    public List<String> zrandmember(Integer dbIndex, String key, int count) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrandmember");
        if (this.isClusterMode()) {
            return this.getCluster().zrandmember(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrandmember(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset所有成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 成员
     */
    public List<String> zrange(int dbIndex, String key) {
        return this.zrange(dbIndex, key, 0, -1);
    }

    /**
     * 获取zset指定区间成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   起始位置
     * @param end     结束位置
     * @return 成员
     */
    public List<String> zrange(Integer dbIndex, String key, long start, long end) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrange");
        if (this.isClusterMode()) {
            return this.getCluster().zrange(key, start, end);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrange(key, start, end);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除zset成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 受影响的数据数量
     */
    public long zrem(Integer dbIndex, String key, String... members) {
        if (ArrayUtil.isEmpty(members)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrem");
        if (this.isClusterMode()) {
            return this.getCluster().zrem(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrem(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset成员排名
     *
     * @param dbIndex db索引
     * @param key     键
     * @param member  成员
     * @return 排名
     */
    public Long zrank(Integer dbIndex, String key, String member) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrank");
        if (this.isClusterMode()) {
            return this.getCluster().zrank(key, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrank(key, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取zset成员排名，从大小到小排序
     *
     * @param dbIndex db索引
     * @param key     键
     * @param member  成员
     * @return 排名
     */
    public Long zrevrank(Integer dbIndex, String key, String member) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zrevrank");
        if (this.isClusterMode()) {
            return this.getCluster().zrevrank(key, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zrevrank(key, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加zset成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param score   成员
     * @param member  分数
     * @return 受影响的数据数量
     */
    public long zadd(Integer dbIndex, String key, double score, String member) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zadd");
        if (this.isClusterMode()) {
            return this.getCluster().zadd(key, score, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zadd(key, score, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加zset成员
     *
     * @param dbIndex      db索引
     * @param key          键
     * @param scoreMembers 成员及分数
     * @return 受影响的数据数量
     */
    public long zadd(Integer dbIndex, String key, Map<String, Double> scoreMembers) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zadd");
        if (this.isClusterMode()) {
            return this.getCluster().zadd(key, scoreMembers);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zadd(key, scoreMembers);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加zset成员分数
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param increment 添加值
     * @param member    成员
     * @return 成员新分数
     */
    public double zincrby(Integer dbIndex, String key, double increment, String member) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "zincrby");
        if (this.isClusterMode()) {
            return this.getCluster().zincrby(key, increment, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.zincrby(key, increment, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 是否set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param member  成员
     * @return 结果
     */
    public boolean sismember(Integer dbIndex, String key, String member) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sismember");
        if (this.isClusterMode()) {
            return this.getCluster().sismember(key, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sismember(key, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 随机返回set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @return set成员
     */
    public String srandmember(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "srandmember");
        if (this.isClusterMode()) {
            return this.getCluster().srandmember(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.srandmember(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 随机返回set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   返回数量
     * @return set成员
     */
    public List<String> srandmember(Integer dbIndex, String key, int count) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "srandmember");
        if (this.isClusterMode()) {
            return this.getCluster().srandmember(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.srandmember(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取set成员数量
     *
     * @param dbIndex db索引
     * @param key     键
     * @return set成员数量
     */
    public long scard(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "scard");
        if (this.isClusterMode()) {
            return this.getCluster().scard(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.scard(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 受影响的数据数量
     */
    public long sadd(Integer dbIndex, String key, String... members) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sadd");
        if (this.isClusterMode()) {
            return this.getCluster().sadd(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (members == null) {
                members = new String[0];
            }
            return jedis.sadd(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从set头部弹出成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 成员
     */
    public String spop(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "spop");
        if (this.isClusterMode()) {
            return this.getCluster().spop(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.spop(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的差集
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return set差集
     */
    public Set<String> sdiff(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptySet();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sdiff");
        if (this.isClusterMode()) {
            return this.getCluster().sdiff(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sdiff(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的差集，并保存到目标set
     *
     * @param dbIndex db索引
     * @param destkey 目标set
     * @param keys    键
     * @return 受影响的数据数量
     */
    public long sdiffstore(Integer dbIndex, String destkey, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sdiffstore");
        if (this.isClusterMode()) {
            return this.getCluster().sdiffstore(destkey, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sdiffstore(destkey, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的并集
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return set并集
     */
    public Set<String> sunion(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptySet();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sunion");
        if (this.isClusterMode()) {
            return this.getCluster().sunion(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sunion(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的并集，并保存到目标set
     *
     * @param dbIndex db索引
     * @param destkey 目标set
     * @param keys    键
     * @return 受影响的数据数量
     */
    public long sunionstore(Integer dbIndex, String destkey, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sunionstore");
        if (this.isClusterMode()) {
            return this.getCluster().sunionstore(destkey, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sunionstore(destkey, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的交集
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return set交集
     */
    public Set<String> sinter(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptySet();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sinter");
        if (this.isClusterMode()) {
            return this.getCluster().sinter(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sinter(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个set的交集，并保存到目标set
     *
     * @param dbIndex db索引
     * @param destkey 目标set
     * @param keys    键
     * @return 受影响的数据数量
     */
    public long sinterstore(Integer dbIndex, String destkey, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "sinterstore");
        if (this.isClusterMode()) {
            return this.getCluster().sinterstore(destkey, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.sinterstore(destkey, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从set头部弹出成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   数量
     * @return 成员
     */
    public Set<String> spop(Integer dbIndex, String key, long count) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "spop");
        if (this.isClusterMode()) {
            return this.getCluster().spop(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.spop(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 受影响的数据数量
     */
    public long srem(Integer dbIndex, String key, String... members) {
        if (ArrayUtil.isEmpty(members)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "srem");
        if (this.isClusterMode()) {
            return this.getCluster().srem(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.srem(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取set成员
     *
     * @param dbIndex db索引
     * @param key     键
     * @return set成员
     */
    public Set<String> smembers(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "smembers");
        if (this.isClusterMode()) {
            return this.getCluster().smembers(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.smembers(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从右侧推入数据到list集合
     *
     * @param dbIndex db索引
     * @param key     键
     * @param values  值
     * @return 受影响数据数量
     */
    public long rpush(Integer dbIndex, String key, String... values) {
        if (ArrayUtil.isEmpty(values)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "rpush");
        if (this.isClusterMode()) {
            return this.getCluster().rpush(key, values);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.rpush(key, values);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从右侧推入数据到list集合(列表需要存在)
     *
     * @param dbIndex db索引
     * @param key     键
     * @param values  值
     * @return 受影响数据数量
     */
    public long rpushx(Integer dbIndex, String key, String... values) {
        if (ArrayUtil.isEmpty(values)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "rpushx");
        if (this.isClusterMode()) {
            return this.getCluster().rpushx(key, values);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.rpushx(key, values);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从左侧推入数据到list集合
     *
     * @param dbIndex db索引
     * @param key     键
     * @param values  值
     * @return 受影响数据数量
     */
    public long lpush(Integer dbIndex, String key, String... values) {
        if (ArrayUtil.isEmpty(values)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lpush");
        if (this.isClusterMode()) {
            return this.getCluster().lpush(key, values);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lpush(key, values);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从左侧推入数据到list集合(列表需要存在)
     *
     * @param dbIndex db索引
     * @param key     键
     * @param values  值
     * @return 受影响数据数量
     */
    public long lpushx(Integer dbIndex, String key, String... values) {
        if (ArrayUtil.isEmpty(values)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lpushx");
        if (this.isClusterMode()) {
            return this.getCluster().lpushx(key, values);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lpushx(key, values);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置list指定索引的值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param index   索引
     * @param value   值
     * @return 结果
     */
    public String lset(Integer dbIndex, String key, int index, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lset");
        if (this.isClusterMode()) {
            return this.getCluster().lset(key, index, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lset(key, index, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 往list指定值位置插入值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param where   位置
     * @param pivot   指定值
     * @param value   插入值
     * @return 受影响的数据数量
     */
    public long linsert(Integer dbIndex, String key, ListPosition where, String pivot, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "linsert");
        if (this.isClusterMode()) {
            return this.getCluster().linsert(key, where, pivot, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.linsert(key, where, pivot, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list头部弹出值，直到超时
     *
     * @param dbIndex db索引
     * @param timeout 位置
     * @param keys    键
     * @return 值
     */
    public List<String> blpop(Integer dbIndex, int timeout, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "blpop");
        if (this.isClusterMode()) {
            return this.getCluster().blpop(timeout, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.blpop(timeout, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list尾部弹出值，直到超时
     *
     * @param dbIndex db索引
     * @param timeout 位置
     * @param keys    键
     * @return 值
     */
    public List<String> brpop(Integer dbIndex, int timeout, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "brpop");
        if (this.isClusterMode()) {
            return this.getCluster().brpop(timeout, keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.brpop(timeout, keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除list指定值，仅删除1个
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @return 受影响的值数量
     */
    public long lrem(int dbIndex, String key, String value) {
        return this.lrem(dbIndex, key, 1, value);
    }

    /**
     * 删除list指定值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   删除个数
     * @param value   值
     * @return 受影响的值数量
     */
    public long lrem(Integer dbIndex, String key, long count, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lrem");
        if (this.isClusterMode()) {
            return this.getCluster().lrem(key, count, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lrem(key, count, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取list所有值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值
     */
    public List<String> lrange(int dbIndex, String key) {
        return this.lrange(dbIndex, key, 0, -1);
    }

    /**
     * 获取list指定区间内的值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   起始位置
     * @param end     结束位置
     * @return 值
     */
    public List<String> lrange(Integer dbIndex, String key, long start, long end) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lrange");
        if (this.isClusterMode()) {
            return this.getCluster().lrange(key, start, end);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lrange(key, start, end);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取list指定索引处的值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param index   索引
     * @return 值
     */
    public String lindex(Integer dbIndex, String key, long index) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lindex");
        if (this.isClusterMode()) {
            return this.getCluster().lindex(key, index);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lindex(key, index);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list头部弹出值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值
     */
    public String lpop(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lpop");
        if (this.isClusterMode()) {
            return this.getCluster().lpop(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lpop(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list头部弹出值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   数量
     * @return 值
     */
    public List<String> lpop(Integer dbIndex, String key, int count) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lpop");
        if (this.isClusterMode()) {
            return this.getCluster().lpop(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.lpop(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list尾部弹出值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值
     */
    public String rpop(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "rpop");
        if (this.isClusterMode()) {
            return this.getCluster().rpop(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.rpop(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 从list尾部部弹出值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param count   数量
     * @return 值
     */
    public List<String> rpop(Integer dbIndex, String key, int count) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "rpop");
        if (this.isClusterMode()) {
            return this.getCluster().rpop(key, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.rpop(key, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 剪切list
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   开始位置
     * @param stop    结束位置
     * @return 结果
     */
    public String ltrim(Integer dbIndex, String key, long start, long stop) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "ltrim");
        if (this.isClusterMode()) {
            return this.getCluster().ltrim(key, start, stop);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.ltrim(key, start, stop);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取list列表的数据个数
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 数据个数
     */
    public long llen(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "llen");
        if (this.isClusterMode()) {
            return this.getCluster().llen(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.llen(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加hyperloglog统计值
     *
     * @param dbIndex  db索引
     * @param key      键
     * @param elements 统计元素
     * @return 当前统计值
     */
    public long pfadd(Integer dbIndex, String key, String... elements) {
        if (ArrayUtil.isEmpty(elements)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pfadd");
        if (this.isClusterMode()) {
            return this.getCluster().pfadd(key, elements);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.pfadd(key, elements);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取hyperloglog的统计值
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 统计值
     */
    public long pfcount(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pfcount");
        if (this.isClusterMode()) {
            return this.getCluster().pfcount(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.pfcount(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 合并多个hyperloglog
     *
     * @param dbIndex    db索引
     * @param destKey    目标键
     * @param sourceKeys 来源键
     * @return 结果
     */
    public String pfmerge(Integer dbIndex, String destKey, String... sourceKeys) {
        if (ArrayUtil.isEmpty(sourceKeys)) {
            return null;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pfmerge");
        if (this.isClusterMode()) {
            return this.getCluster().pfmerge(destKey, sourceKeys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.pfmerge(destKey, sourceKeys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加geo坐标
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param longitude 经度
     * @param latitude  纬度
     * @param member    成员
     * @return 受影响的数据数量
     */
    public long geoadd(Integer dbIndex, String key, double longitude, double latitude, String member) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "geoadd");
        if (this.isClusterMode()) {
            return this.getCluster().geoadd(key, longitude, latitude, member);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.geoadd(key, longitude, latitude, member);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加geo坐标
     *
     * @param dbIndex          db索引
     * @param params           参数
     * @param key              键
     * @param memberCoordinate 成员及坐标
     * @return 受影响的数据数量
     */
    public long geoadd(Integer dbIndex, String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinate) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "geoadd");
        if (this.isClusterMode()) {
            return this.getCluster().geoadd(key, params, memberCoordinate);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (params == null) {
                return jedis.geoadd(key, memberCoordinate);
            }
            return jedis.geoadd(key, params, memberCoordinate);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取geo坐标hash值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 成员hash值
     */
    public List<String> geohash(Integer dbIndex, String key, String... members) {
        if (ArrayUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "geohash");
        if (this.isClusterMode()) {
            return this.getCluster().geohash(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.geohash(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 计算geo两个坐标的距离
     *
     * @param dbIndex db索引
     * @param key     键
     * @param member1 成员1
     * @param member2 成员2
     * @param unit    距离单位
     * @return 距离
     */
    public Double geodist(Integer dbIndex, String key, String member1, String member2, GeoUnit unit) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "geodist");
        if (this.isClusterMode()) {
            if (unit == null) {
                return this.getCluster().geodist(key, member1, member2);
            }
            return this.getCluster().geodist(key, member1, member2, unit);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (unit == null) {
                return jedis.geodist(key, member1, member2);
            }
            return jedis.geodist(key, member1, member2, unit);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取geo坐标的经纬度
     *
     * @param dbIndex db索引
     * @param key     键
     * @param members 成员
     * @return 经纬度
     */

    public List<GeoCoordinate> geopos(Integer dbIndex, String key, String... members) {
        if (ArrayUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "geopos");
        if (this.isClusterMode()) {
            return this.getCluster().geopos(key, members);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.geopos(key, members);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 键是否存在
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 结果
     */
    public boolean exists(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "exists");
        if (this.isClusterMode()) {
            return this.getCluster().exists(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.exists(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 键是否存在
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 结果
     */
    public long exists(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "exists");
        if (this.isClusterMode()) {
            return this.getCluster().exists(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.exists(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @return 结果
     */
    public String set(Integer dbIndex, String key, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "set");
        if (this.isClusterMode()) {
            return this.getCluster().set(key, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.set(key, value == null ? "" : value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @return 结果
     */
    public String set(Integer dbIndex, byte[] key, byte[] value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "set");
        if (this.isClusterMode()) {
            return this.getCluster().set(key, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.set(key, value == null ? new byte[]{} : value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string指定位置值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param offset  位置
     * @param value   值
     * @return 受影响的数据数量
     */
    public long setrange(Integer dbIndex, String key, long offset, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "setrange");
        if (this.isClusterMode()) {
            return this.getCluster().setrange(key, offset, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.setrange(key, offset, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string值，仅键不存在时
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @return 结果
     */
    public long setnx(Integer dbIndex, String key, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "setnx");
        if (this.isClusterMode()) {
            return this.getCluster().setnx(key, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.setnx(key, value == null ? "" : value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string值并更新ttl
     *
     * @param dbIndex db索引
     * @param key     键
     * @param seconds 剩余存活时间
     * @param value   值
     * @return 结果
     */
    public String setex(Integer dbIndex, String key, long seconds, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "setex");
        if (this.isClusterMode()) {
            return this.getCluster().setex(key, seconds, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.setex(key, seconds, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置多个string值
     *
     * @param dbIndex   db索引
     * @param keyValues 键及值
     * @return 结果
     */
    public String mset(Integer dbIndex, String... keyValues) {
        if (ArrayUtil.isEmpty(keyValues)) {
            return null;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "mset");
        if (this.isClusterMode()) {
            return this.getCluster().mset(keyValues);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.mset(keyValues);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置多个string值，仅键不存在时
     *
     * @param dbIndex   db索引
     * @param keyValues 键及值
     * @return 受影响的数据数量
     */
    public long msetnx(Integer dbIndex, String... keyValues) {
        if (ArrayUtil.isEmpty(keyValues)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "msetnx");
        if (this.isClusterMode()) {
            return this.getCluster().msetnx(keyValues);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.msetnx(keyValues);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值
     */
    public String get(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "get");
        if (this.isClusterMode()) {
            return this.getCluster().get(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.get(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值
     */
    public byte[] get(Integer dbIndex, byte[] key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "get");
        if (this.isClusterMode()) {
            return this.getCluster().get(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.get(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取string指定区间值
     *
     * @param dbIndex     db索引
     * @param key         键
     * @param startOffset 开始位置
     * @param endOffset   结束位置
     * @return 值
     */
    public String getrange(Integer dbIndex, String key, long startOffset, long endOffset) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "getrange");
        if (this.isClusterMode()) {
            return this.getCluster().getrange(key, startOffset, endOffset);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.getrange(key, startOffset, endOffset);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取多个string值
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 值
     */
    public List<String> mget(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "mget");
        if (this.isClusterMode()) {
            return this.getCluster().mget(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.mget(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 追加string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   追加内容
     * @return 受影响的数据数量
     */
    public long append(Integer dbIndex, String key, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "append");
        if (this.isClusterMode()) {
            return this.getCluster().append(key, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.append(key, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取string值长度
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 值长度
     */
    public long strlen(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "strlen");
        if (this.isClusterMode()) {
            return this.getCluster().strlen(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.strlen(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置string值，并返回旧值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @return 旧值
     */
    public String getSet(Integer dbIndex, String key, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "getSet");
        if (this.isClusterMode()) {
            return this.getCluster().getSet(key, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.getSet(key, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 减少string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 当前值
     */
    public long decr(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "decr");
        if (this.isClusterMode()) {
            return this.getCluster().decr(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.decr(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 减少string值
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param decrement 减少值
     * @return 当前值
     */
    public long decrBy(Integer dbIndex, String key, long decrement) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "decrBy");
        if (this.isClusterMode()) {
            return this.getCluster().decrBy(key, decrement);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.decrBy(key, decrement);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加string值
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 当前值
     */
    public long incr(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "incr");
        if (this.isClusterMode()) {
            return this.getCluster().incr(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.incr(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加string值
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param increment 增加值
     * @return 当前值
     */
    public long incrBy(Integer dbIndex, String key, long increment) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "incrBy");
        if (this.isClusterMode()) {
            return this.getCluster().incrBy(key, increment);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.incrBy(key, increment);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 增加string值，以浮点形式
     *
     * @param dbIndex   db索引
     * @param key       键
     * @param increment 增加值
     * @return 当前值
     */
    public double incrByFloat(Integer dbIndex, String key, double increment) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "incrByFloat");
        if (this.isClusterMode()) {
            return this.getCluster().incrByFloat(key, increment);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.incrByFloat(key, increment);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置bit值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param offset  位置
     * @param value   值
     * @return 当前值
     */
    public boolean setbit(Integer dbIndex, String key, long offset, boolean value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "setbit");
        if (this.isClusterMode()) {
            return this.getCluster().setbit(key, offset, value);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.setbit(key, offset, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取bit值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param offset  位置
     * @return 当前值
     */
    public boolean getbit(Integer dbIndex, String key, long offset) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "getbit");
        if (this.isClusterMode()) {
            return this.getCluster().getbit(key, offset);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.getbit(key, offset);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 统计bit值
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   开始位置
     * @param end     结束位置
     * @param option  操作
     * @return 值为true的数量
     */
    public long bitcount(Integer dbIndex, String key, Long start, Long end, BitCountOption option) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "bitcount");
        if (this.isClusterMode()) {
            if (start == null || end == null) {
                return this.getCluster().bitcount(key);
            }
            if (option == null) {
                return this.getCluster().bitcount(key, start, end);
            }
            return this.getCluster().bitcount(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (start == null || end == null) {
                return jedis.bitcount(key);
            }
            if (option == null) {
                return jedis.bitcount(key, start, end);
            }
            return jedis.bitcount(key, start, end, option);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取bit值首次出现的位置
     *
     * @param dbIndex db索引
     * @param key     键
     * @param value   值
     * @param params  参数
     * @return 值首次出现的位置
     */
    public long bitpos(Integer dbIndex, String key, boolean value, BitPosParams params) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "bitpos");
        if (this.isClusterMode()) {
            if (params == null) {
                return this.getCluster().bitpos(key, value);
            }
            return this.getCluster().bitpos(key, value, params);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (params == null) {
                return jedis.bitpos(key, value);
            }
            return jedis.bitpos(key, value, params);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除键
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 删除数量
     */
    public long del(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "del");
        if (this.isClusterMode()) {
            return this.getCluster().del(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.del(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除键
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 删除数量
     */
    public long del(Integer dbIndex, Collection<String> keys) {
        if (CollectionUtil.isNotEmpty(keys)) {
            this.del(dbIndex, ArrayUtil.toArray(keys, String.class));
        }
        return -1L;
    }

    /**
     * 添加stream消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @param id      流id
     * @param hash    字段及值
     * @return 流id
     */
    public StreamEntryID xadd(Integer dbIndex, String key, StreamEntryID id, Map<String, String> hash) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xadd");
        if (this.isClusterMode()) {
            return this.getCluster().xadd(key, id, hash);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.xadd(key, id, hash);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 添加stream消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @param hash    字段及值
     * @param params  参数
     * @return 流id
     */
    public StreamEntryID xadd(Integer dbIndex, String key, Map<String, String> hash, XAddParams params) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xadd");
        if (this.isClusterMode()) {
            return this.getCluster().xadd(key, hash, params);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.xadd(key, hash, params);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 删除stream消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @param ids     流id
     * @return 受影响的数据数量
     */
    public long xdel(Integer dbIndex, String key, StreamEntryID... ids) {
        if (ArrayUtil.isEmpty(ids)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xdel");
        if (this.isClusterMode()) {
            return this.getCluster().xdel(key, ids);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.xdel(key, ids);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取stream信息
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 受影响的数据数量
     */
    public StreamInfo xinfoStream(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xinfoStream");
        if (this.isClusterMode()) {
            return this.getCluster().xinfoStream(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.xinfoStream(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取stream信息
     *
     * @param dbIndex db索引
     * @param key     键
     * @return stream信息
     */
    public StreamFullInfo xinfoStreamFull(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xinfoStreamFull");
        if (this.isClusterMode()) {
            return this.getCluster().xinfoStreamFull(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.xinfoStreamFull(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取stream的所有消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 消息
     */
    public List<StreamEntry> xrange(Integer dbIndex, String key) {
        return this.xrange(dbIndex, key, null, null, null);
    }

    /**
     * 获取stream区间内的消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   流开始id
     * @return 消息
     */
    public List<StreamEntry> xrange(Integer dbIndex, String key, StreamEntryID start) {
        return this.xrange(dbIndex, key, start, null, null);
    }

    /**
     * 获取stream区间内的消息
     *
     * @param dbIndex db索引
     * @param key     键
     * @param start   流开始id
     * @param end     流结束id
     * @param count   获取数量
     * @return 消息
     */
    public List<StreamEntry> xrange(Integer dbIndex, String key, StreamEntryID start, StreamEntryID end, Integer count) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "xrange");
        if (this.isClusterMode()) {
            if (count == null) {
                return this.getCluster().xrange(key, start, end);
            }
            return this.getCluster().xrange(key, start, end, count);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (count == null) {
                return jedis.xrange(key, start, end);
            }
            return jedis.xrange(key, start, end, count);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取随机key
     *
     * @param dbIndex db索引
     * @return 结果
     */
    public String randomKey(Integer dbIndex) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "randomKey");
        if (this.isClusterMode()) {
            return this.getCluster().randomKey();
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.randomKey();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 移动键
     *
     * @param key           键名称
     * @param formDBIndex   来源db索引
     * @param targetDBIndex 目标db索引
     * @return 结果
     */
    public long move(String key, Integer formDBIndex, int targetDBIndex) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "move");
        if (this.isClusterMode()) {
            throw new ClusterOperationException();
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, formDBIndex);
            return jedis.move(key, targetDBIndex);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键类型
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 键类型
     */
    public String type(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "type");
        if (this.isClusterMode()) {
            return this.getCluster().type(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.type(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键类型，支持多个
     *
     * @param dbIndex db索引
     * @param keys    键列表
     * @return 键类型列表
     */
    public List<String> typeMulti(Integer dbIndex, Collection<String> keys) {
        if (CollectionUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "type");
        // 类型集合
        List<String> types = new ArrayList<>(keys.size());
        // cluster集群处理
        if (this.isClusterMode()) {
            try (ClusterPipeline pipeline = this.getCluster().pipelined()) {
                List<Response<String>> list = new ArrayList<>(keys.size());
                for (String key : keys) {
                    list.add(pipeline.type(key));
                }
                pipeline.sync();
                for (Response<String> response : list) {
                    types.add(response.get());
                }
            }
        } else {// 一般连接处理
            Jedis jedis = this.getResource(dbIndex);
            try (Pipeline pipeline = jedis.pipelined()) {
                this.dbIndex(jedis, dbIndex);
                for (String key : keys) {
                    pipeline.type(key);
                }
                types = (List) pipeline.syncAndReturnAll();
            } finally {
                this.returnResource(jedis);
            }
        }
        return types;
    }

    /**
     * 获取键数量
     *
     * @param dbIndex db索引
     * @return 键数量
     */
    public long dbSize(Integer dbIndex) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "dbSize");
        long dbsize = 0;
        // 集群模式
        if (this.isClusterMode()) {
            // 合并搜索结果
            for (ConnectionPool connectionPool : this.getClusterPools()) {
                dbsize += this.dbSize(connectionPool);
            }
        } else {// 普通模式
            Jedis jedis = this.getResource(dbIndex);
            try {
                this.dbIndex(jedis, dbIndex);
                dbsize = jedis.dbSize();
            } finally {
                this.returnResource(jedis);
            }
        }
        return dbsize;
    }

    /**
     * 获取键数量
     *
     * @param pool 连接池
     * @return 键数量
     */
    public long dbSize(ConnectionPool pool) {
        Connection connection = pool.getResource();
        try {
            return connection.executeCommand(this.commandObjects.dbSize());
        } finally {
            pool.returnResource(connection);
        }
    }

    /**
     * 扫描键
     *
     * @param dbIndex db索引
     * @param cursor  光标
     * @param params  扫描参照
     * @return 扫描结果
     */
    public ScanResult<String> scan(int dbIndex, String cursor, ScanParams params) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "scan");
        if (cursor == null) {
            cursor = ScanParams.SCAN_POINTER_START;
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.scan(cursor, params);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 列举键
     *
     * @param dbIndex db索引
     * @param pattern 查找模板
     * @return 键列表
     */
    public Set<String> keys(Integer dbIndex, String pattern) {
        return this.keys(dbIndex, pattern, null);
    }

    /**
     * 列举键
     *
     * @param pool    连接池
     * @param pattern 查找模板
     * @return 键列表
     */
    public Set<String> keys(ConnectionPool pool, String pattern) {
        Connection connection = pool.getResource();
        try {
            // 搜索单个节点内匹配的Key
            return connection.executeCommand(this.commandObjects.keys(pattern));
        } finally {
            pool.returnResource(connection);
        }
    }

    /**
     * 列举键
     *
     * @param dbIndex db索引
     * @param pattern 查找模板
     * @param type    键类型
     * @return 键列表
     */
    public Set<String> keys(Integer dbIndex, String pattern, ShellRedisKeyType type) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "keys");
        Set<String> keys;
        // 集群模式
        if (this.isClusterMode()) {
            keys = new HashSet<>();
            // 合并搜索结果
            for (ConnectionPool connectionPool : this.getClusterPools()) {
                keys.addAll(this.keys(connectionPool, pattern));
            }
        } else {// 普通模式
            Jedis jedis = this.getResource(dbIndex);
            try {
                this.dbIndex(jedis, dbIndex);
                keys = jedis.keys(pattern);
            } finally {
                this.returnResource(jedis);
            }
        }
        if (type != null && CollectionUtil.isNotEmpty(keys)) {
            Set<String> set = new HashSet<>();
            for (String key : keys) {
                if (type.equalsString(this.type(dbIndex, key))) {
                    set.add(key);
                }
            }
            return set;
        }
        return keys;
    }

    /**
     * 重命名键
     *
     * @param dbIndex db索引
     * @param key     键
     * @param newKey  新键名称
     * @return 结果
     */
    public String rename(Integer dbIndex, String key, String newKey) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "rename");
        if (this.isClusterMode()) {
            return this.getCluster().rename(key, newKey);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.rename(key, newKey);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取存活时间
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 剩余存活时间
     */
    public long ttl(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "ttl");
        if (this.isClusterMode()) {
            return this.getCluster().ttl(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.ttl(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 以毫秒值获取存活时间
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 剩余存活时间毫秒值
     */
    public long pttl(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pttl");
        if (this.isClusterMode()) {
            return this.getCluster().pttl(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.pttl(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置过期时间
     *
     * @param dbIndex db索引
     * @param key     键
     * @param seconds 过期时间，单位秒
     * @param option  过期操作
     * @return 受影响的键数量
     */
    public long expire(Integer dbIndex, String key, long seconds, ExpiryOption option) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "expire");
        if (this.isClusterMode()) {
            if (option == null) {
                return this.getCluster().expire(key, seconds);
            }
            return this.getCluster().expire(key, seconds, option);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (option == null) {
                return jedis.expire(key, seconds);
            }
            return jedis.expire(key, seconds, option);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置过期时间，以毫秒为单位
     *
     * @param dbIndex      db索引
     * @param key          键
     * @param milliseconds 过期时间，单位毫秒
     * @param option       过期操作
     * @return 受影响的键数量
     */
    public long pexpire(Integer dbIndex, String key, long milliseconds, ExpiryOption option) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pexpire");
        if (this.isClusterMode()) {
            return this.getCluster().pexpire(key, milliseconds, option);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (option == null) {
                return jedis.pexpire(key, milliseconds);
            }
            return jedis.pexpire(key, milliseconds, option);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置到期时间
     *
     * @param dbIndex  db索引
     * @param key      键
     * @param unixTime unix时间
     * @param option   过期操作
     * @return 受影响的键数量
     */
    public long expireAt(Integer dbIndex, String key, long unixTime, ExpiryOption option) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "expireAt");
        if (this.isClusterMode()) {
            return this.getCluster().expireAt(key, unixTime, option);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (option == null) {
                return jedis.expireAt(key, unixTime);
            }
            return jedis.expireAt(key, unixTime, option);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置到期时间，以毫秒为单位
     *
     * @param dbIndex               db索引
     * @param key                   键
     * @param millisecondsTimestamp unix时间毫秒值
     * @param option                过期操作
     * @return 受影响的键数量
     */
    public long pexpireAt(Integer dbIndex, String key, long millisecondsTimestamp, ExpiryOption option) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pexpireAt");
        if (this.isClusterMode()) {
            return this.getCluster().expireAt(key, millisecondsTimestamp, option);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (option == null) {
                return jedis.pexpireAt(key, millisecondsTimestamp);
            }
            return jedis.pexpireAt(key, millisecondsTimestamp, option);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 键持久化
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 受影响的键数量
     */
    public long persist(Integer dbIndex, String key) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "persist");
        if (this.isClusterMode()) {
            return this.getCluster().persist(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.persist(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 修改键的最后访问时间
     *
     * @param dbIndex db索引
     * @param keys    键
     * @return 受影响的键数量
     */
    public long touch(Integer dbIndex, String... keys) {
        if (ArrayUtil.isEmpty(keys)) {
            return -1L;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "touch");
        if (this.isClusterMode()) {
            return this.getCluster().touch(keys);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.touch(keys);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 等待副本写入
     *
     * @param replicas 副本数量
     * @param timeout  等待时间
     * @return 完成的键数量
     */
    public long waitReplicas(int replicas, long timeout) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "wait");
        if (this.isClusterMode()) {
            return this.getCluster().waitReplicas((String) null, replicas, timeout);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.waitReplicas(replicas, timeout);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 等待副本保存
     *
     * @param numLocal 本地数量
     * @param replicas 副本数量
     * @param timeout  等待时间
     * @return 完成的键数量
     */
    public KeyValue<Long, Long> waitAOF(long numLocal, int replicas, long timeout) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "waitAOF");
        if (this.isClusterMode()) {
            return this.getCluster().waitAOF((String) null, numLocal, replicas, timeout);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.waitAOF(numLocal, replicas, timeout);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键编码信息
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 键编码信息
     */
    public String objectEncoding(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "object encoding");
        if (this.isClusterMode()) {
            return this.getCluster().objectEncoding(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.objectEncoding(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键访问频率
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 键访问频率
     */
    public Long objectFreq(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "object freq");
        if (this.isClusterMode()) {
            return this.getCluster().objectFreq(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.objectFreq(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键空闲时间
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 键访问频率
     */
    public Long objectIdletime(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "object idletime");
        if (this.isClusterMode()) {
            return this.getCluster().objectIdletime(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.objectIdletime(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取键引用数量
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 键访问频率
     */
    public Long objectRefcount(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "object refcount");
        if (this.isClusterMode()) {
            return this.getCluster().objectRefcount(key);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.objectRefcount(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 复制键
     *
     * @param dbIndex db索引
     * @param srcKey  来源键名称
     * @param dstKey  目标键名称
     * @param db      目标db索引
     * @param replace 是否替换
     * @return 结果
     */
    public boolean copy(Integer dbIndex, String srcKey, String dstKey, Integer db, boolean replace) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "copy");
        if (this.isClusterMode()) {
            return this.getCluster().copy(srcKey, dstKey, replace);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            if (db == null) {
                return jedis.copy(srcKey, dstKey, replace);
            }
            return jedis.copy(srcKey, dstKey, db, replace);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 清空数据库
     *
     * @param dbIndex db索引
     * @return 结果
     */
    public String flushDB(Integer dbIndex) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "flushDB");
        if (this.isClusterMode()) {
            return this.getCluster().flushDB();
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.flushDB();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 清空所有数据
     *
     * @return 结果
     */
    public String flushAll() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "flushAll");
        if (this.isClusterMode()) {
            return this.getCluster().flushAll();
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.flushAll();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取配置
     *
     * @param pattern 查找模板
     * @return 配置列表
     */
    public Map<String, String> configGet(String pattern) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "config get");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.configGet(pattern);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 更改配置
     *
     * @param parameter 配置
     * @param value     值
     * @return 结果
     */
    public String configSet(String parameter, String value) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "config set");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.configSet(parameter, value);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 更改配置
     *
     * @param parameterValues 配置及值
     * @return 结果
     */
    public String configSet(String... parameterValues) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "config set");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.configSet(parameterValues);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 配置改写到redis.conf文件
     *
     * @return 结果
     */
    public String configRewrite() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "config rewrite");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.configRewrite();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 重置部分统计信息
     *
     * @return 结果
     */
    public String configResetStat() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "config resetStat");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.configResetStat();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取连接所有键
     *
     * @param pattern 键模式
     * @return 键列表
     */
    public Map<Integer, Set<String>> fullKeys(String pattern) {
        Map<Integer, Set<String>> keys = new HashMap<>();
        for (int i = 0; i < this.databases(); i++) {
            Set<String> set = this.keys(i, pattern);
            if (CollectionUtil.isNotEmpty(set)) {
                keys.put(i, set);
            }
        }
        return keys;
    }

    /**
     * 获取所有键
     *
     * @param dbIndex db索引
     * @param pattern 键模式
     * @return 键列表
     */
    public Set<String> allKeys(Integer dbIndex, String pattern) {
        return this.keys(dbIndex, pattern);
    }

    /**
     * 切换db索引
     *
     * @param jedis   jedis连接
     * @param dbIndex db索引
     * @return 切换结果
     */
    private String dbIndex(Jedis jedis, Integer dbIndex) {
        if (jedis == null || this.isClusterMode() || this.isSentinelMode()) {
            return null;
        }
        if (dbIndex == null) {
            if (jedis.getDB() != this.dbIndex) {
                return jedis.select(this.dbIndex);
            }
        } else {
            if (dbIndex < 0 || dbIndex > this.databases()) {
                throw new InvalidParameterException("dbIndex in 0~" + this.databases());
            }
            if (this.dbIndex != dbIndex) {
                this.dbIndex = dbIndex;
            }
            if (jedis.getDB() != dbIndex) {
                return jedis.select(dbIndex);
            }
        }
        return "OK";
    }

    /**
     * 切换数据库
     *
     * @param dbIndex db索引
     * @return 结果
     */
    public String select(Integer dbIndex) {
        this.throwSentinelException();
        if (this.isClusterMode()) {
            throw new ClusterOperationException();
        }
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "select");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return this.dbIndex(jedis, dbIndex);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 发布消息
     *
     * @param channel 通道
     * @param message 消息
     * @return 结果
     */
    public long publish(String channel, String message) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "publish");
        if (this.isClusterMode()) {
            return this.getCluster().publish(channel, message);
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.publish(channel, message);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取通道的订阅数量
     *
     * @param channels 通道
     * @return 结果
     */
    public Map<String, Long> pubsubNumSub(String... channels) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pubsub numSub");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.pubsubNumSub(channels);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取订阅及发布的活跃通道数量
     *
     * @return 活跃通道数量
     */
    public Long pubsubNumPat() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pubsub numPat");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.pubsubNumPat();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 订阅消息
     *
     * @param pubSub   消息订阅组件
     * @param channels 通道列表
     */
    public void subscribe(JedisPubSub pubSub, String... channels) {
        if (ArrayUtil.isEmpty(channels)) {
            return;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "subscribe");
        if (this.isClusterMode()) {
            this.getCluster().subscribe(pubSub, channels);
        } else {
            Jedis jedis = this.getResource(dbIndex);
            jedis.subscribe(pubSub, channels);
        }
    }

    /**
     * 订阅消息
     *
     * @param pubSub   消息订阅组件
     * @param patterns 订阅模式
     */
    public void psubscribe(JedisPubSub pubSub, String... patterns) {
        if (ArrayUtil.isEmpty(patterns)) {
            return;
        }
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "psubscribe");
        if (this.isClusterMode()) {
            this.getCluster().subscribe(pubSub, patterns);
        } else {
            Jedis jedis = this.getResource(dbIndex);
            jedis.psubscribe(pubSub, patterns);
        }
    }

    /**
     * 获取发布列表
     *
     * @param pattern 模式
     * @return 发布列表
     */
    public List<String> pubsubChannels(String pattern) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "pubsub channels");
        Jedis jedis = this.getResource(dbIndex);
        try {
            if (pattern == null) {
                return jedis.pubsubChannels();
            }
            return jedis.pubsubChannels(pattern);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取db数量
     *
     * @return db数量
     */
    public Integer databases() {
        if (this.databases != null) {
            return this.databases;
        }
        try {
            if (!this.isClusterMode() && !this.isSentinelMode()) {
                Map<String, String> config = this.configGet("databases");
                if (CollectionUtil.isEmpty(config)) {
                    this.databases = 16;
                } else {
                    this.databases = Integer.parseInt(CollectionUtil.getFirst(config.values()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.databases = 16;
        }
        return this.databases;
    }

    /**
     * 获取info属性
     *
     * @return info属性
     */
    public RedisInfoProp infoProp() {
        if (this.infoProp == null) {
            this.infoProp = new RedisInfoProp();
        }
        if (this.infoProp.isEmpty()) {
            this.infoProp.parse(this.info(null));
        }
        return this.infoProp;
    }

    /**
     * 清理info属性
     */
    public void clearInfoProp() {
        if (this.infoProp != null) {
            this.infoProp = null;
        }
    }

    /**
     * 获取服务信息
     *
     * @param section 信息段
     * @return 结果
     */
    public String info(String section) {
        Jedis jedis = this.getResource(dbIndex);
        try {
            if (section == null) {
                return jedis.info();
            }
            return jedis.info(section);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取当前db索引
     *
     * @return db索引
     */
    public int getDB() {
        return this.dbIndex;
    }

    /**
     * 获取服务器时间
     *
     * @return 结果
     */
    public List<String> time() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "time");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.time();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 将数据保存到磁盘
     *
     * @return 结果
     */
    public String save() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "save");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.save();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取最后一次保存数据到磁盘的时间
     *
     * @return 结果
     */
    public long lastsave() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "lastsave");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.lastsave();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 将数据异步保存到磁盘
     *
     * @return 结果
     */
    public String bgsave() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "bgsave");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.bgsave();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 执行aof文件重写
     *
     * @return 结果
     */
    public String bgrewriteaof() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "bgrewriteaof");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.bgrewriteaof();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取连接列表
     *
     * @return 结果
     */
    public String clientList() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "client list");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.clientList();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取连接名称
     *
     * @return 结果
     */
    public String clientGetname() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "client getname");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.clientGetname();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 设置连接名称
     *
     * @param name 名称
     * @return 结果
     */
    public String clientSetname(String name) {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "client setname");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.clientSetname(name);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取慢查日志数量
     *
     * @return 慢查日志数量
     */
    public long slowlogLen() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "slowlog len");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.slowlogLen();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 清空慢查日志
     *
     * @return 结果
     */
    public String slowlogReset() {
        this.throwSentinelException();
        this.throwReadonlyException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "slowlog reset");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.slowlogReset();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取全部慢查日志
     *
     * @return 慢查日志
     */
    public List<Slowlog> slowlogGet() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "slowlog get");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.slowlogGet();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取慢查日志
     *
     * @param entries 获取数量
     * @return 慢查日志
     */
    public List<Slowlog> slowlogGet(long entries) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "slowlog get");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.slowlogGet(entries);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取内存占用
     *
     * @param dbIndex db索引
     * @param key     键
     * @return 内存占用
     */
    public Long memoryUsage(Integer dbIndex, String key) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "memoryUsage");
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.memoryUsage(key);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取当前服务的角色
     *
     * @return 当前服务的角色
     */
    public List<Object> role() {
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "role");
        Jedis jedis = this.getResource(dbIndex);
        try {
            return jedis.role();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取当前服务的角色
     *
     * @param pool 连接池
     * @return 当前服务的角色
     */
    public List<Object> role(ConnectionPool pool) {
        Connection connection = pool.getResource();
        try {
            connection.sendCommand(Protocol.Command.ROLE);
            return BuilderFactory.ENCODED_OBJECT_LIST.build(connection.getOne());
        } finally {
            pool.returnResource(connection);
        }
    }

    /**
     * 获取命令总数
     *
     * @return 获取命令总数
     */
    public long commandCount() {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "command count");
        Jedis jedis = this.getResource();
        try {
            return jedis.commandCount();
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 获取命令信息
     *
     * @param commands 命令
     * @return 命令信息
     */
    public Map<String, CommandInfo> commandInfo(String... commands) {
        this.throwSentinelException();
        ShellRedisVersionUtil.checkSupported(this.getServerVersion(), "command info");
        Jedis jedis = this.getResource();
        try {
            return jedis.commandInfo(commands);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 执行命令
     *
     * @param dbIndex       库
     * @param commandObject 命令对象
     * @return 结果
     */
    public Object execCommand(Integer dbIndex, CommandObject<Object> commandObject) {
        if (commandObject == null) {
            return null;
        }
        Jedis jedis = this.getResource(dbIndex);
        try {
            this.dbIndex(jedis, dbIndex);
            return jedis.getConnection().executeCommand(commandObject);
        } finally {
            this.returnResource(jedis);
        }
    }

    /**
     * 执行命令
     *
     * @param commandObject 命令对象
     * @return 结果
     */
    public Object execCommand(CommandObject<Object> commandObject) {
        return this.execCommand(null, commandObject);
    }

    /**
     * 获取服务端版本号
     *
     * @return 服务端版本号
     */
    public String getServerVersion() {
        return this.infoProp().getRedisVersion();
    }

    /**
     * 当前连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.shellConnect.getName();
    }

    public String iid() {
        return this.shellConnect.getId();
    }

    /**
     * 执行查询
     *
     * @param param 查询参数
     * @return 查询结果
     */
    public ShellRedisQueryResult query(ShellRedisQueryParam param) {
        ShellRedisQueryResult result = new ShellRedisQueryResult();
        long start = System.currentTimeMillis();
        try {
            TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(param.getContent());
            if (handler instanceof RedisTerminalCommandHandler<?> commandHandler) {
                TerminalCommand command = commandHandler.parseCommand(param.getContent());
                CommandObject<Object> object = RedisTerminalUtil.getCommand(commandHandler.getCommandType(), command.getArgs());
                Object execResult = this.execCommand(param.getDbIndex(), object);
                result.setResult(execResult);
            }
            result.setSuccess(true);
            result.setMessage("OK");
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
        }
        long end = System.currentTimeMillis();
        result.setCost(end - start);
        return result;
    }

//
//    public List<RedisScript> listScripts() {
//        List<RedisScript> result = new ArrayList<>();
//        try (Jedis jedis = this.getResource()) {
//            List<Object> response = (List<Object>) jedis.sendCommand(Protocol.Command.SCRIPT, "LIST");
//            // 解析返回结果
//            for (Object scriptObj : response) {
//                List<Object> scriptInfo = (List<Object>) scriptObj;
//                String sha1 = SafeEncoder.encode((byte[]) scriptInfo.getFirst());
//                RedisScript script = new RedisScript();
//                script.setSha1(sha1);
//                System.out.println("---------------------------");
//                System.out.println("SHA1: " + sha1);
//                // Redis 6.2+ 可能返回脚本内容（索引 1）
//                if (scriptInfo.size() > 1) {
//                    String content = SafeEncoder.encode((byte[]) scriptInfo.get(1));
//                    script.setScript(content);
//                    System.out.println("Script Content:\n" + content);
//                } else {
//                    System.out.println("Script content not available (Redis < 6.2).");
//                }
//                result.add(script);
//            }
//        }
//        return result;
//
//    }
//
//    public boolean deleteScript(String sha1) {
//        return false;
//    }
}
