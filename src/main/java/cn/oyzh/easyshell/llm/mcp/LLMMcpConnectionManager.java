package cn.oyzh.easyshell.llm.mcp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MCP 短期连接与工具列表缓存。
 *
 * <p>相同配置在短时间内复用 MCP 客户端，避免每次 LLM 请求都重新启动进程或建立网络连接。
 * 配置变化、健康检查失败、缓存过期或 Server 被禁用时会淘汰旧连接。已经借出的旧连接会等待
 * 当前请求释放后再关闭，避免中断正在执行的任务。</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
final class LLMMcpConnectionManager {

    private static final long TOOL_CACHE_TTL_MILLIS = 2 * 60 * 1000L;

    private static final long HEALTH_CHECK_INTERVAL_MILLIS = 15 * 1000L;

    private static final LLMMcpConnectionManager INSTANCE =
            new LLMMcpConnectionManager();

    private final Map<String, CachedConnection> connections = new ConcurrentHashMap<>();

    private final Map<String, Object> connectionLocks = new ConcurrentHashMap<>();

    private final AtomicLong cacheHits = new AtomicLong();

    private LLMMcpConnectionManager() {
    }

    static LLMMcpConnectionManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取一个带引用计数的连接租约。
     *
     * @param configuration Server 配置
     * @return 连接租约
     * @throws Exception 连接失败
     */
    ConnectionLease acquire(LLMMcpServer configuration) throws Exception {
        String connectionKey = LLMMcpClientFactory.clientKey(configuration);
        String fingerprint = fingerprint(configuration);
        CachedConnection cached = this.connections.get(connectionKey);
        if (cached != null) {
            ConnectionLease lease = this.tryAcquire(connectionKey, cached, fingerprint);
            if (lease != null) {
                return lease;
            }
        }

        Object lock = this.connectionLocks.computeIfAbsent(
                connectionKey, ignored -> new Object());
        synchronized (lock) {
            cached = this.connections.get(connectionKey);
            if (cached != null) {
                ConnectionLease lease = this.tryAcquire(connectionKey, cached, fingerprint);
                if (lease != null) {
                    return lease;
                }
            }
            CachedConnection created = this.connect(configuration, fingerprint);
            CachedConnection previous = this.connections.put(connectionKey, created);
            if (previous != null) {
                previous.retire();
            }
            JulLog.debug("mcp connection opened: {}", connectionKey);
            return created.lease();
        }
    }

    /**
     * 淘汰当前请求未包含的 Server 缓存。
     *
     * @param activeKeys 当前启用 Server 的连接键
     */
    void retainOnly(Set<String> activeKeys) {
        Set<String> retained = activeKeys == null ? Set.of() : Set.copyOf(activeKeys);
        this.connections.forEach((key, connection) -> {
            if (!retained.contains(key) && this.connections.remove(key, connection)) {
                JulLog.debug("mcp connection cache removed because server is disabled: {}", key);
                connection.retire();
            }
        });
    }

    /**
     * 关闭全部连接缓存。
     */
    void closeAll() {
        this.connections.forEach((key, connection) -> {
            if (this.connections.remove(key, connection)) {
                JulLog.debug("mcp connection cache closed: {}", key);
                connection.retire();
            }
        });
        this.connectionLocks.clear();
    }

    int cacheSize() {
        return this.connections.size();
    }

    long cacheHits() {
        return this.cacheHits.get();
    }

    private ConnectionLease tryAcquire(String key,
                                       CachedConnection connection,
                                       String fingerprint) {
        synchronized (connection) {
            if (connection.invalid) {
                if (this.connections.remove(key, connection)) {
                    connection.retire();
                }
                return null;
            }
            if (!connection.fingerprint.equals(fingerprint)) {
                JulLog.info("mcp server configuration changed, reconnecting: {}", key);
                if (this.connections.remove(key, connection)) {
                    connection.retire();
                }
                return null;
            }
            if (connection.expired()) {
                JulLog.debug("mcp connection cache expired, reconnecting: {}", key);
                if (this.connections.remove(key, connection)) {
                    connection.retire();
                }
                return null;
            }
            try {
                connection.checkHealth();
            } catch (Exception ex) {
                JulLog.info("mcp connection health check failed, reconnecting: " + key, ex);
                if (this.connections.remove(key, connection)) {
                    connection.retire();
                }
                return null;
            }
            this.cacheHits.incrementAndGet();
            JulLog.debug("mcp connection reused: {}", key);
            return connection.lease();
        }
    }

    private CachedConnection connect(LLMMcpServer configuration,
                                     String fingerprint) throws Exception {
        McpClient client = null;
        try {
            client = LLMMcpClientFactory.createClient(configuration);
            List<ToolSpecification> tools = client.listTools();
            Set<String> allowedTools = LLMMcpConfigParser.parseToolNames(
                    configuration.getToolNamesText());
            boolean resources = supportsResources(client);
            return new CachedConnection(configuration, fingerprint, client,
                    tools, allowedTools, resources);
        } catch (Exception ex) {
            IOUtil.closeQuietly(client);
            throw ex;
        }
    }

    private static boolean supportsResources(McpClient client) {
        try {
            client.listResources();
            client.listResourceTemplates();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String fingerprint(LLMMcpServer server) {
        String value = String.join("\u0000",
                value(server.getName()),
                String.valueOf(server.getTransportType()),
                value(server.getCommandText()),
                value(server.getWorkingDirectory()),
                value(server.getEnvironmentText()),
                value(server.getUrl()),
                value(server.getHeadersText()),
                value(server.getToolNamesText()),
                String.valueOf(server.getTimeoutSeconds()));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    /**
     * 连接租约，使用完毕后释放引用计数。
     */
    interface ConnectionLease extends AutoCloseable {

        McpClient client();

        LLMMcpServer configuration();

        List<ToolSpecification> tools();

        Set<String> allowedTools();

        boolean supportsResources();

        @Override
        void close();
    }

    private final class CachedConnection {

        private final LLMMcpServer configuration;

        private final String fingerprint;

        private final McpClient client;

        private final List<ToolSpecification> tools;

        private final Set<String> allowedTools;

        private final boolean supportsResources;

        private final AtomicInteger leases = new AtomicInteger();

        private final long createdAt = System.currentTimeMillis();

        private volatile long lastHealthCheck;

        private volatile boolean invalid;

        private CachedConnection(LLMMcpServer configuration,
                                 String fingerprint,
                                 McpClient client,
                                 List<ToolSpecification> tools,
                                 Set<String> allowedTools,
                                 boolean supportsResources) {
            this.configuration = configuration;
            this.fingerprint = fingerprint;
            this.client = client;
            this.tools = List.copyOf(tools);
            this.allowedTools = Set.copyOf(allowedTools);
            this.supportsResources = supportsResources;
        }

        private boolean expired() {
            return System.currentTimeMillis() - this.createdAt
                    >= TOOL_CACHE_TTL_MILLIS;
        }

        private void checkHealth() {
            long now = System.currentTimeMillis();
            if (now - this.lastHealthCheck < HEALTH_CHECK_INTERVAL_MILLIS) {
                return;
            }
            this.client.checkHealth();
            this.lastHealthCheck = now;
        }

        private ConnectionLease lease() {
            this.leases.incrementAndGet();
            return new Lease(this);
        }

        private synchronized void release() {
            if (this.leases.decrementAndGet() == 0 && this.invalid) {
                IOUtil.closeQuietly(this.client);
            }
        }

        private synchronized void retire() {
            this.invalid = true;
            if (this.leases.get() == 0) {
                IOUtil.closeQuietly(this.client);
            }
        }
    }

    private final class Lease implements ConnectionLease {

        private final CachedConnection connection;

        private final AtomicBoolean closed = new AtomicBoolean();

        private Lease(CachedConnection connection) {
            this.connection = connection;
        }

        @Override
        public McpClient client() {
            return this.connection.client;
        }

        @Override
        public LLMMcpServer configuration() {
            return this.connection.configuration;
        }

        @Override
        public List<ToolSpecification> tools() {
            return this.connection.tools;
        }

        @Override
        public Set<String> allowedTools() {
            return this.connection.allowedTools;
        }

        @Override
        public boolean supportsResources() {
            return this.connection.supportsResources;
        }

        @Override
        public void close() {
            if (this.closed.compareAndSet(false, true)) {
                this.connection.release();
            }
        }
    }
}
