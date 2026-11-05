package cn.oyzh.easyshell.internal;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.domain.redis.ShellRedisCollect;
import cn.oyzh.easyshell.domain.zk.ShellZKCollect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSession;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.LLMToolAudit;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.local.ShellLocalCommandExecutor;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellFileCollectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.easyshell.store.ShellJumpConfigStore;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.store.ShellProxyConfigStore;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.store.ShellSSLConfigStore;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.easyshell.store.ShellTerminalHistoryStore;
import cn.oyzh.easyshell.store.ShellTunnelingConfigStore;
import cn.oyzh.easyshell.store.ShellX11ConfigStore;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.store.redis.RedisCollectStore;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.easyshell.store.zk.ShellZKCollectStore;
import cn.oyzh.easyshell.store.zk.ShellZKSASLConfigStore;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.util.FXUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fazecast.jSerialComm.SerialPort;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import javafx.scene.control.Tab;
import javafx.scene.text.Font;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellLLMTools implements LLMTools {

    private ShellTabPane tabPane;

    private final ShellLocalCommandExecutor localCommandExecutor;

    public ShellLLMTools() {
        this.localCommandExecutor = new ShellLocalCommandExecutor();
    }

    public ShellLLMTools(ShellTabPane tabPane) {
        this();
        this.tabPane = tabPane;
    }

    public void setTabPane(ShellTabPane tabPane) {
        this.tabPane = tabPane;
    }

    @Tool(name = "getProjectInfo", value = "Get EasyShell project metadata")
    public String getProjectInfo() {
        try {
            Project project = Project.load();
            if (project == null) {
                return "Project metadata is unavailable.";
            }
            StringBuilder result = new StringBuilder();
            this.append(result, "name", project.getName());
            this.append(result, "type", project.getType());
            this.append(result, "version", project.getVersion());
            this.append(result, "updateDate", project.getUpdateDate());
            this.append(result, "copyright", project.getCopyright());
            this.append(result, "implementationTitle", this.value(
                    ShellLLMTools.class.getPackage().getImplementationTitle()));
            this.append(result, "implementationVersion", this.value(
                    ShellLLMTools.class.getPackage().getImplementationVersion()));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get project info", exception);
        }
    }

    @Tool(name = "getProjectVersion", value = "Get the EasyShell application version")
    public String getProjectVersion() {
        try {
            Project project = Project.load();
            return "projectVersion: " + this.value(project == null ? null : project.getVersion());
        } catch (Exception exception) {
            return this.failure("Get project version", exception);
        }
    }

    @Tool(name = "getJdkVersion", value = "Get the Java runtime and JDK version used by EasyShell")
    public String getJdkVersion() {
        try {
            Runtime.Version runtimeVersion = Runtime.version();
            StringBuilder result = new StringBuilder();
            this.append(result, "javaVersion", runtimeVersion.toString());
            this.append(result, "javaFeature", runtimeVersion.feature());
            this.append(result, "javaRuntimeVersion", this.property("java.runtime.version"));
            this.append(result, "javaVmName", this.property("java.vm.name"));
            this.append(result, "javaVmVersion", this.property("java.vm.version"));
            this.append(result, "javaVendor", this.property("java.vendor"));
            this.append(result, "javaHome", this.property("java.home"));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get JDK version", exception);
        }
    }

    @Tool(name = "getRuntimeInfo", value = "Get process, operating system, memory, locale, and timezone information")
    public String getRuntimeInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
            long uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(
                    ManagementFactory.getRuntimeMXBean().getUptime());
            StringBuilder result = new StringBuilder();
            this.append(result, "pid", ProcessHandle.current().pid());
            this.append(result, "uptimeSeconds", uptimeSeconds);
            this.append(result, "osName", this.property("os.name"));
            this.append(result, "osVersion", this.property("os.version"));
            this.append(result, "osArch", this.property("os.arch"));
            this.append(result, "availableProcessors", runtime.availableProcessors());
            this.append(result, "heapUsed", this.formatBytes(heapUsage.getUsed()));
            this.append(result, "heapCommitted", this.formatBytes(heapUsage.getCommitted()));
            this.append(result, "heapMax", this.formatBytes(heapUsage.getMax()));
            this.append(result, "nonHeapUsed", this.formatBytes(nonHeapUsage.getUsed()));
            this.append(result, "threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
            this.append(result, "locale", Locale.getDefault().toString());
            this.append(result, "timezone", TimeZone.getDefault().getID());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get runtime info", exception);
        }
    }

    @Tool(name = "getProjectPaths", value = "Get EasyShell data, cache, key cache, node cache, log paths, and LLM tool audit file path")
    public String getProjectPaths() {
        try {
            Path databasePath = Path.of(ShellConst.getStorePath(), "db");
            StringBuilder result = new StringBuilder();
            this.append(result, "storeDirectory", ShellConst.getStorePath());
            this.append(result, "cacheDirectory", ShellConst.getCachePath());
            this.append(result, "keyCacheDirectory", ShellConst.getKeyCachePath());
            this.append(result, "nodeCacheDirectory", ShellConst.getNodeCachePath());
            this.append(result, "logDirectory", this.value(JulUtil.getLogsDir()));
            this.append(result, "llmToolAuditFile", this.value(LLMToolAudit.defaultAuditFile()));
            this.append(result, "database", this.describePath(databasePath));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get project paths", exception);
        }
    }

    @Tool(name = "getStoredDataSummary", value = "Get counts of locally stored EasyShell data without exposing record contents")
    public String getStoredDataSummary() {
        StringBuilder result = new StringBuilder();
        this.appendCount(result, "keys", () -> ShellKeyStore.INSTANCE.selectList().size());
        this.appendCount(result, "groups", () -> ShellGroupStore.INSTANCE.selectList().size());
        this.appendCount(result, "queries", () -> ShellQueryStore.INSTANCE.selectList().size());
        this.appendCount(result, "snippets", () -> ShellSnippetStore.INSTANCE.selectList().size());
        this.appendCount(result, "llmSessions", () -> LLMSessionStore.INSTANCE.selectList().size());
        this.appendCount(result, "connections", () -> ShellConnectStore.INSTANCE.selectList().size());
        this.appendCount(result, "llmProviders", () -> LLMProviderStore.INSTANCE.selectList().size());
        this.appendCount(result, "sslConfigs", () -> ShellSSLConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "x11Configs", () -> ShellX11ConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "zkAuthConfigs", () -> ShellZKAuthStore.INSTANCE.selectList().size());
        this.appendCount(result, "jumpConfigs", () -> ShellJumpConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "proxyConfigs", () -> ShellProxyConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "fileFavorites", () -> ShellFileCollectStore.INSTANCE.selectList().size());
        this.appendCount(result, "redisFavorites", () -> RedisCollectStore.INSTANCE.selectList().size());
        this.appendCount(result, "zkFavorites", () -> ShellZKCollectStore.INSTANCE.selectList().size());
        this.appendCount(result, "zkSaslConfigs", () -> ShellZKSASLConfigStore.INSTANCE.selectList().size());
        this.appendCount(result, "terminalHistory", () -> ShellTerminalHistoryStore.INSTANCE.selectList().size());
        this.appendCount(result, "tunnelingConfigs", () -> ShellTunnelingConfigStore.INSTANCE.selectList().size());
        return result.toString();
    }

    @Tool(name = "getApplicationCapabilities", value = "List EasyShell connection protocols and supported feature areas")
    public String getApplicationCapabilities() {
        return LLMUtil.readLlmText("internal_capabilities.txt");
    }

    @Tool(name = "listSupportedProtocols", value = "List all supported connection protocols and their feature categories")
    public String listSupportedProtocols() {
        return LLMUtil.readLlmText("internal_protocols.txt");
    }

    @Tool(name = "listSavedConnections", value = "List saved EasyShell connections without passwords, keys, tokens, or certificates")
    public String listSavedConnections(
            @P(name = "filter", value = "Optional case-insensitive text filter for name, type, host, or remark") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<ShellConnect> connections = new ArrayList<>(ShellConnectStore.INSTANCE.selectList());
            connections.sort(Comparator.comparing(ShellConnect::getName, String.CASE_INSENSITIVE_ORDER));
            Map<String, String> groupNames = this.groupNames();
            List<ShellConnect> matched = connections.stream()
                    .filter(connection -> this.matchesConnection(connection, filter))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellConnect connection = matched.get(index);
                result.append("id: ").append(this.value(connection.getId()))
                        .append(" | name: ").append(this.value(connection.getName()))
                        .append(" | type: ").append(this.value(connection.getType()))
                        .append(" | host: ").append(this.value(connection.hostIp()))
                        .append(" | port: ").append(connection.hostPort())
                        .append(" | group: ").append(this.value(groupNames.get(connection.getGroupId())))
                        .append(" | charset: ").append(this.value(connection.getCharset()))
                        .append(" | authConfigured: ").append(this.hasAuthentication(connection))
                        .append(" | remark: ").append(this.value(connection.getRemark()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List saved connections", exception);
        }
    }

    @Tool(name = "listOpenConnections", value = "List connection tabs currently open in EasyShell with live connection state")
    public String listOpenConnections(
            @P(name = "filter", value = "Optional case-insensitive text filter for tab title, name, type, or host") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<OpenConnectionSnapshot> connections = this.openConnections();
            List<OpenConnectionSnapshot> matched = connections.stream()
                    .filter(connection -> this.matchesOpenConnection(connection, filter))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                OpenConnectionSnapshot connection = matched.get(index);
                result.append("tabId: ").append(this.value(connection.tabId()))
                        .append(" | selected: ").append(connection.selected())
                        .append(" | tabTitle: ").append(this.value(connection.title()))
                        .append(" | connectionId: ").append(this.value(connection.connect().getId()))
                        .append(" | name: ").append(this.value(connection.connect().getName()))
                        .append(" | type: ").append(this.value(connection.connect().getType()))
                        .append(" | host: ").append(this.value(connection.connect().hostIp()))
                        .append(" | port: ").append(connection.connect().hostPort())
                        .append(" | state: ").append(this.value(connection.state()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List open connections", exception);
        }
    }

    @Tool(name = "listGroups", value = "List saved connection groups and their connection counts")
    public String listGroups(
            @P(name = "filter", value = "Optional case-insensitive group name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 500 and capped at 1000") int maxResults) {
        try {
            List<ShellGroup> groups = new ArrayList<>(ShellGroupStore.INSTANCE.selectList());
            groups.sort(Comparator.comparing(ShellGroup::getName, String.CASE_INSENSITIVE_ORDER));
            Map<String, Integer> connectionCounts = new LinkedHashMap<>();
            for (ShellConnect connection : ShellConnectStore.INSTANCE.selectList()) {
                connectionCounts.merge(connection.getGroupId(), 1, Integer::sum);
            }
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellGroup> matched = groups.stream()
                    .filter(group -> query.isBlank() || this.value(group.getName()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 500, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellGroup group = matched.get(index);
                result.append("id: ").append(this.value(group.getGid()))
                        .append(" | parentId: ").append(this.value(group.getPid()))
                        .append(" | name: ").append(this.value(group.getName()))
                        .append(" | expanded: ").append(group.isExpand())
                        .append(" | connections: ").append(connectionCounts.getOrDefault(group.getGid(), 0))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List groups", exception);
        }
    }

    @Tool(name = "listSnippets", value = "List saved terminal snippets without returning their full content")
    public String listSnippets(
            @P(name = "filter", value = "Optional case-insensitive snippet name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<ShellSnippet> snippets = new ArrayList<>(ShellSnippetStore.INSTANCE.selectList());
            snippets.sort(Comparator.comparing(ShellSnippet::getName, String.CASE_INSENSITIVE_ORDER));
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellSnippet> matched = snippets.stream()
                    .filter(snippet -> query.isBlank()
                            || this.value(snippet.getName()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellSnippet snippet = matched.get(index);
                result.append("id: ").append(this.value(snippet.getId()))
                        .append(" | name: ").append(this.value(snippet.getName()))
                        .append(" | contentLength: ").append(this.length(snippet.getContent()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List snippets", exception);
        }
    }

    @Tool(name = "listSavedKeys", value = "List saved SSH key metadata without exposing private keys, public keys, or passwords")
    public String listSavedKeys(
            @P(name = "filter", value = "Optional case-insensitive key name or type filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            List<ShellKey> keys = new ArrayList<>(ShellKeyStore.INSTANCE.selectList());
            keys.sort(Comparator.comparing(ShellKey::getName, String.CASE_INSENSITIVE_ORDER));
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellKey> matched = keys.stream()
                    .filter(key -> query.isBlank()
                            || this.value(key.getName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(key.getType()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellKey key = matched.get(index);
                result.append("id: ").append(this.value(key.getId()))
                        .append(" | name: ").append(this.value(key.getName()))
                        .append(" | type: ").append(this.value(key.getType()))
                        .append(" | length: ").append(key.getLength())
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List saved keys", exception);
        }
    }

    @Tool(name = "listQueries", value = "List saved query metadata without returning SQL content")
    public String listQueries(
            @P(name = "filter", value = "Optional case-insensitive query name, connection name, or database filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            Map<String, String> connectionNames = this.connectionNames();
            List<ShellQuery> queries = new ArrayList<>(ShellQueryStore.INSTANCE.selectList());
            queries.sort(Comparator.comparing(ShellQuery::getName, String.CASE_INSENSITIVE_ORDER));
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellQuery> matched = queries.stream()
                    .filter(item -> query.isBlank()
                            || this.value(item.getName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(connectionNames.get(item.getIid())).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(item.getDbName()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellQuery item = matched.get(index);
                result.append("id: ").append(this.value(item.getUid()))
                        .append(" | name: ").append(this.value(item.getName()))
                        .append(" | connectionId: ").append(this.value(item.getIid()))
                        .append(" | connection: ").append(this.value(connectionNames.get(item.getIid())))
                        .append(" | database: ").append(this.value(item.getDbName()))
                        .append(" | dbIndex: ").append(item.getDbIndex())
                        .append(" | contentLength: ").append(this.length(item.getContent()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List queries", exception);
        }
    }

    @Tool(name = "listFileFavorites", value = "List saved file or directory favorites without changing remote files or project data")
    public String listFileFavorites(
            @P(name = "filter", value = "Optional case-insensitive path or connection name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            Map<String, String> connectionNames = this.connectionNames();
            List<ShellFileCollect> favorites = new ArrayList<>(ShellFileCollectStore.INSTANCE.selectList());
            favorites.sort(Comparator.comparingLong(ShellFileCollect::getSaveTime).reversed());
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellFileCollect> matched = favorites.stream()
                    .filter(favorite -> query.isBlank()
                            || this.value(favorite.getContent()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(connectionNames.get(favorite.getIid())).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellFileCollect favorite = matched.get(index);
                result.append("id: ").append(this.value(favorite.getId()))
                        .append(" | connectionId: ").append(this.value(favorite.getIid()))
                        .append(" | connection: ").append(this.value(connectionNames.get(favorite.getIid())))
                        .append(" | path: ").append(this.value(favorite.getContent()))
                        .append(" | saveTime: ").append(favorite.getSaveTime())
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List file favorites", exception);
        }
    }

    @Tool(name = "listRedisFavorites", value = "List saved Redis key favorites without reading or changing the keys")
    public String listRedisFavorites(
            @P(name = "filter", value = "Optional case-insensitive key or connection name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            Map<String, String> connectionNames = this.connectionNames();
            List<ShellRedisCollect> favorites = new ArrayList<>(RedisCollectStore.INSTANCE.selectList());
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellRedisCollect> matched = favorites.stream()
                    .filter(favorite -> query.isBlank()
                            || this.value(favorite.getKey()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(connectionNames.get(favorite.getIid())).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellRedisCollect favorite = matched.get(index);
                result.append("id: ").append(this.value(favorite.getUid()))
                        .append(" | connectionId: ").append(this.value(favorite.getIid()))
                        .append(" | connection: ").append(this.value(connectionNames.get(favorite.getIid())))
                        .append(" | dbIndex: ").append(favorite.getDbIndex())
                        .append(" | key: ").append(this.value(favorite.getKey()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List Redis favorites", exception);
        }
    }

    @Tool(name = "listZkFavorites", value = "List saved ZooKeeper node favorites without reading or changing node data")
    public String listZkFavorites(
            @P(name = "filter", value = "Optional case-insensitive path or connection name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            Map<String, String> connectionNames = this.connectionNames();
            List<ShellZKCollect> favorites = new ArrayList<>(ShellZKCollectStore.INSTANCE.selectList());
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ShellZKCollect> matched = favorites.stream()
                    .filter(favorite -> query.isBlank()
                            || this.value(favorite.getPath()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(connectionNames.get(favorite.getIid())).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellZKCollect favorite = matched.get(index);
                result.append("connectionId: ").append(this.value(favorite.getIid()))
                        .append(" | connection: ").append(this.value(connectionNames.get(favorite.getIid())))
                        .append(" | path: ").append(this.value(favorite.getPath()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List ZooKeeper favorites", exception);
        }
    }

    @Tool(name = "getSnippet", value = "Read one saved terminal snippet by id. This is read-only and truncates long content.")
    public String getSnippet(
            @P(name = "snippetId", value = "Snippet id returned by listSnippets") String snippetId,
            @P(name = "maxChars", value = "Maximum content characters, default 65536 and capped at 262144") int maxChars) {
        try {
            ShellSnippet snippet = this.findSnippet(snippetId);
            if (snippet == null) {
                return "Snippet not found: " + this.value(snippetId);
            }
            StringBuilder result = new StringBuilder();
            this.append(result, "id", snippet.getId());
            this.append(result, "name", snippet.getName());
            this.append(result, "contentLength", this.length(snippet.getContent()));
            result.append("content:\n").append(this.truncate(snippet.getContent(), maxChars));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get snippet", exception);
        }
    }

    @Tool(name = "getQuery", value = "Read one saved query by id without changing it. This is read-only and truncates long content.")
    public String getQuery(
            @P(name = "queryId", value = "Query id returned by listQueries") String queryId,
            @P(name = "maxChars", value = "Maximum content characters, default 65536 and capped at 262144") int maxChars) {
        try {
            ShellQuery query = this.findQuery(queryId);
            if (query == null) {
                return "Query not found: " + this.value(queryId);
            }
            Map<String, String> connectionNames = this.connectionNames();
            StringBuilder result = new StringBuilder();
            this.append(result, "id", query.getUid());
            this.append(result, "name", query.getName());
            this.append(result, "connectionId", query.getIid());
            this.append(result, "connection", connectionNames.get(query.getIid()));
            this.append(result, "database", query.getDbName());
            this.append(result, "dbIndex", query.getDbIndex());
            this.append(result, "contentLength", this.length(query.getContent()));
            result.append("content:\n").append(this.truncate(query.getContent(), maxChars));
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get query", exception);
        }
    }

    @Tool(name = "listLlmProviders", value = "List configured LLM providers without exposing API keys")
    public String listLlmProviders(
            @P(name = "filter", value = "Optional case-insensitive provider name, base URL, API type, or model filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 100 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<LLMProvider> providers = new ArrayList<>(LLMProviderStore.INSTANCE.selectList());
            providers.sort(Comparator.comparing(LLMProvider::getName, String.CASE_INSENSITIVE_ORDER));
            List<LLMProvider> matched = providers.stream()
                    .filter(provider -> query.isBlank()
                            || this.value(provider.getName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(provider.getBaseUrl()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(provider.getApiTypeName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(provider.getDefaultModel()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 100, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                LLMProvider provider = matched.get(index);
                result.append("id: ").append(this.value(provider.getUid()))
                        .append(" | name: ").append(this.value(provider.getName()))
                        .append(" | apiType: ").append(this.value(provider.getApiTypeName()))
                        .append(" | active: ").append(provider.isActive())
                        .append(" | baseUrl: ").append(this.value(provider.getBaseUrl()))
                        .append(" | defaultModel: ").append(this.value(provider.getDefaultModel()))
                        .append(" | modelCount: ").append(provider.getModels().size())
                        .append(" | apiKeyConfigured: ").append(!this.value(provider.getSk()).equals("-"))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List LLM providers", exception);
        }
    }

    @Tool(name = "listLlmSessions", value = "List saved LLM session metadata without exposing message content")
    public String listLlmSessions(
            @P(name = "filter", value = "Optional case-insensitive session title filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<LLMSession> sessions = new ArrayList<>(LLMSessionStore.INSTANCE.selectList());
            sessions.sort(Comparator.comparingLong(LLMSession::getUpdateTime).reversed());
            List<LLMSession> matched = sessions.stream()
                    .filter(session -> query.isBlank()
                            || this.value(session.getTitle()).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                LLMSession session = matched.get(index);
                result.append("id: ").append(this.value(session.getUid()))
                        .append(" | title: ").append(this.value(session.getTitle()))
                        .append(" | temporary: ").append(session.isTemporary())
                        .append(" | messageCount: ").append(this.messageCount(session))
                        .append(" | createTime: ").append(session.getCreateTime())
                        .append(" | updateTime: ").append(session.getUpdateTime())
                        .append(" | updateTimeText: ").append(this.value(session.getUpdateTimeText()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List LLM sessions", exception);
        }
    }

    @Tool(name = "getConnectionDetails", value = "Get non-sensitive details for one saved EasyShell connection")
    public String getConnectionDetails(
            @P(name = "connectionId", value = "Connection id returned by listSavedConnections") String connectionId) {
        try {
            ShellConnect connection = this.findFullConnection(connectionId);
            if (connection == null) {
                return "Connection not found: " + this.value(connectionId);
            }
            Map<String, String> groupNames = this.groupNames();
            StringBuilder result = new StringBuilder();
            this.append(result, "id", connection.getId());
            this.append(result, "name", connection.getName());
            this.append(result, "type", connection.getType());
            this.append(result, "host", connection.getHost());
            this.append(result, "ip", connection.hostIp());
            this.append(result, "port", connection.hostPort());
            this.append(result, "group", groupNames.get(connection.getGroupId()));
            this.append(result, "user", connection.getUser());
            this.append(result, "authConfigured", this.hasAuthentication(connection));
            this.append(result, "keyId", connection.getKeyId());
            this.append(result, "authMethod", connection.getAuthMethod());
            this.append(result, "charset", connection.getCharset());
            this.append(result, "osType", connection.getOsType());
            this.append(result, "termType", connection.getTermType());
            this.append(result, "backspaceType", connection.getBackspaceType());
            this.append(result, "altSendsEscape", connection.isAltSendsEscape());
            this.append(result, "environmentConfigured", connection.getEnvironment() != null
                    && !connection.getEnvironment().isBlank());
            this.append(result, "connectTimeOutSeconds", connection.getConnectTimeOut());
            this.append(result, "executeTimeOutSeconds", connection.getExecuteTimeOut());
            this.append(result, "readonly", connection.isReadonly());
            this.append(result, "sslMode", connection.isSSLMode());
            this.append(result, "ftpPassiveMode", connection.isFtpPassiveMode());
            this.append(result, "forwardAgent", connection.isForwardAgent());
            this.append(result, "x11forwarding", connection.isX11forwarding());
            this.append(result, "enableCompress", connection.isEnableCompress());
            this.append(result, "enableZModem", connection.isEnableZModem());
            this.append(result, "showFile", connection.isShowFile());
            this.append(result, "serverMonitor", connection.isServerMonitor());
            this.append(result, "followTerminalDir", connection.isFollowTerminalDir());
            this.append(result, "showHiddenFile", connection.isShowHiddenFile());
            this.append(result, "region", connection.getRegion());
            this.append(result, "s3Type", connection.getS3Type());
            this.append(result, "smbShareName", connection.getSmbShareName());
            this.append(result, "domain", connection.getDomain());
            this.append(result, "mongoAuthDatabase", connection.getMongoAuthDatabase());
            this.append(result, "mongoSpecifiedDatabase", connection.getMongoSpecifiedDatabase());
            this.append(result, "saslAuth", connection.isSASLAuth());
            this.append(result, "sessionTimeOutMinutes", connection.getSessionTimeOut());
            this.append(result, "zkCompatibility", connection.getCompatibility());
            this.append(result, "serialPortName", connection.getSerialPortName());
            this.append(result, "serialBaudRate", connection.getSerialBaudRate());
            this.append(result, "serialDataBits", connection.getSerialNumDataBits());
            this.append(result, "serialStopBits", connection.getSerialNumStopBits());
            this.append(result, "serialParityBits", connection.getSerialParityBits());
            this.append(result, "serialFlowControl", connection.getSerialFlowControl());
            this.append(result, "certificateConfigured", connection.getCertificate() != null
                    && !connection.getCertificate().isBlank());
            this.append(result, "moshKeyConfigured", connection.getMoshKey() != null
                    && !connection.getMoshKey().isBlank());
            this.append(result, "proxyEnabled", connection.isEnableProxy());
            this.append(result, "jumpConfigs", connection.getJumpConfigs() == null ? 0 : connection.getJumpConfigs().size());
            this.append(result, "tunnelingConfigs", connection.getTunnelingConfigs() == null ? 0 : connection.getTunnelingConfigs().size());
            this.append(result, "remark", connection.getRemark());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get connection details", exception);
        }
    }

    @Tool(name = "createConnection",
            value = "Create and persist a new EasyShell connection without connecting to it. Existing connections are never modified or deleted.")
    public String createConnection(
            @P(name = "type", value = "Connection type: ssh, sftp, ftp, s3, webdav, smb, local, telnet, serial, rlogin, mosh, rdp, vnc, mysql, mariadb, dameng, sqlite, mongodb, redis, or zookeeper") String type,
            @P(name = "name", value = "Display name for the new connection") String name,
            @P(name = "host", value = "Host or address. For SQLite use the database file path. Ignored for Local and Serial.") String host,
            @P(name = "port", value = "Port, ignored when the host already contains a port or for Local, Serial, and SQLite", required = false) Integer port,
            @P(name = "user", value = "Optional login user name", required = false) String user,
            @P(name = "password", value = "Optional login password. Never returned by any tool.", required = false) String password,
            @P(name = "groupId", value = "Optional existing group id; empty means the root group", required = false) String groupId,
            @P(name = "remark", value = "Optional non-sensitive remark", required = false) String remark,
            @P(name = "charset", value = "Optional charset, for example UTF-8", required = false) String charset,
            @P(name = "keyId", value = "Optional saved key id returned by listSavedKeys", required = false) String keyId,
            @P(name = "authMethod", value = "Optional SSH auth method such as password, certificate, manager, or sshAgent", required = false) String authMethod,
            @P(name = "readonly", value = "Optional readonly flag for protocols that support it", required = false) Boolean readonly,
            @P(name = "connectTimeoutSeconds", value = "Optional connection timeout in seconds, default 15 and capped at 600", required = false) Integer connectTimeoutSeconds,
            @P(name = "sslMode", value = "Optional SSL flag for protocols that support it", required = false) Boolean sslMode,
            @P(name = "typeOptions", value = "Optional JSON object for type-specific settings. Supported keys: osType, termType, backspaceType (0-2), altSendsEscape, environment, ftpPassiveMode, showHiddenFile, region, s3Type, smbShareName, domain, mongoAuthDatabase, mongoSpecifiedDatabase, executeTimeoutSeconds, serialPortName, serialBaudRate, serialDataBits, serialStopBits, serialParity (none/even/odd/mark/space), serialFlowControl (none/rtscts/xonxoff), saslAuth, sessionTimeoutMinutes, zkCompatibility, forwardAgent, x11forwarding, showFile, serverMonitor, followTerminalDir, enableCompress, enableZModem", required = false) String typeOptions) {
        try {
            String connectionType = this.normalizeConnectionType(type);
            String connectionName = name == null ? "" : name.trim();
            if (connectionName.isBlank()) {
                return "Connection name is required.";
            }
            String rawHost = host == null ? "" : host.trim();
            boolean hostRequired = !connectionType.equals("local") && !connectionType.equals("serial");
            if (hostRequired && rawHost.isBlank()) {
                return "Host is required for connection type: " + connectionType;
            }
            if (port != null && port <= 0) {
                port = null;
            }
            if (port != null && port > 65535) {
                return "Port must be between 1 and 65535.";
            }
            String resolvedGroupId = this.requireExistingGroupId(groupId);
            String resolvedKeyId = null;
            if (keyId != null && !keyId.isBlank()) {
                resolvedKeyId = keyId.trim();
                if (!ShellKeyStore.INSTANCE.exist(resolvedKeyId)) {
                    return "Key not found: " + resolvedKeyId;
                }
            }
            ShellConnect connection = new ShellConnect();
            connection.setType(connectionType);
            connection.setName(connectionName);
            connection.setHost(this.buildConnectionHost(rawHost, port, connectionType));
            if (user != null && !user.isBlank()) {
                connection.setUser(user.trim());
            }
            if (password != null && !password.isEmpty()) {
                connection.setPassword(password);
            }
            connection.setGroupId(resolvedGroupId);
            if (remark != null && !remark.isBlank()) {
                connection.setRemark(remark.trim());
            }
            if (charset != null && !charset.isBlank()) {
                connection.setCharset(charset.trim());
            }
            if (resolvedKeyId != null) {
                connection.setKeyId(resolvedKeyId);
            }
            if (authMethod != null && !authMethod.isBlank()) {
                connection.setAuthMethod(authMethod.trim());
            } else if (resolvedKeyId != null) {
                connection.setAuthMethod("manager");
            } else if (password != null && !password.isEmpty()
                    && (connectionType.equals("ssh") || connectionType.equals("mosh"))) {
                connection.setAuthMethod("password");
            }
            connection.setReadonly(Boolean.TRUE.equals(readonly));
            connection.setSSLMode(Boolean.TRUE.equals(sslMode));
            this.applyTypeOptions(connection, connectionType, rawHost, typeOptions);
            int timeout = connectTimeoutSeconds == null || connectTimeoutSeconds <= 0
                    ? 15 : Math.min(connectTimeoutSeconds, 600);
            connection.setConnectTimeOut(timeout);
            if (!ShellConnectStore.INSTANCE.insert(connection)) {
                return "Connection creation failed.";
            }
            ShellEventUtil.connectAdded(connection);
            return "Created connection:\n" + this.connectionSummary(connection, this.groupNames());
        } catch (Exception exception) {
            return this.failure("Create connection", exception);
        }
    }

    @Tool(name = "renameConnection", value = "Rename one saved EasyShell connection without changing any other field")
    public String renameConnection(
            @P(name = "connectionId", value = "Connection id returned by listSavedConnections") String connectionId,
            @P(name = "newName", value = "New non-empty connection name") String newName) {
        try {
            String name = newName == null ? "" : newName.trim();
            if (name.isBlank()) {
                return "New connection name is required.";
            }
            ShellConnect connection = this.findConnection(connectionId);
            if (connection == null) {
                return "Connection not found: " + this.value(connectionId);
            }
            connection.setName(name);
            if (!ShellConnectStore.INSTANCE.update(connection)) {
                return "Connection rename failed.";
            }
            ShellEventUtil.connectUpdated(connection);
            return "Renamed connection:\n" + this.connectionSummary(connection, this.groupNames());
        } catch (Exception exception) {
            return this.failure("Rename connection", exception);
        }
    }

    @Tool(name = "updateConnectionGroup", value = "Move one saved EasyShell connection to another existing group, or to the root when groupId is empty")
    public String updateConnectionGroup(
            @P(name = "connectionId", value = "Connection id returned by listSavedConnections") String connectionId,
            @P(name = "groupId", value = "Target group id returned by listGroups, or empty for the root group") String groupId) {
        try {
            ShellConnect connection = this.findConnection(connectionId);
            if (connection == null) {
                return "Connection not found: " + this.value(connectionId);
            }
            String resolvedGroupId = this.requireExistingGroupId(groupId);
            connection.setGroupId(resolvedGroupId);
            if (!ShellConnectStore.INSTANCE.update(connection)) {
                return "Connection group update failed.";
            }
            ShellEventUtil.connectUpdated(connection);
            return "Updated connection group:\n" + this.connectionSummary(connection, this.groupNames());
        } catch (Exception exception) {
            return this.failure("Update connection group", exception);
        }
    }

    @Tool(name = "updateConnectionRemark", value = "Update the remark of one saved EasyShell connection without changing credentials or connection settings")
    public String updateConnectionRemark(
            @P(name = "connectionId", value = "Connection id returned by listSavedConnections") String connectionId,
            @P(name = "remark", value = "New remark; empty clears the remark") String remark) {
        try {
            ShellConnect connection = this.findConnection(connectionId);
            if (connection == null) {
                return "Connection not found: " + this.value(connectionId);
            }
            connection.setRemark(remark == null || remark.isBlank() ? null : remark.trim());
            if (!ShellConnectStore.INSTANCE.update(connection)) {
                return "Connection remark update failed.";
            }
            ShellEventUtil.connectUpdated(connection);
            return "Updated connection remark:\n" + this.connectionSummary(connection, this.groupNames());
        } catch (Exception exception) {
            return this.failure("Update connection remark", exception);
        }
    }

    @Tool(name = "createGroup", value = "Create and persist a new EasyShell connection group without modifying or deleting existing groups")
    public String createGroup(
            @P(name = "name", value = "New group name") String name,
            @P(name = "parentGroupId", value = "Optional parent group id; empty creates a root-level group", required = false) String parentGroupId) {
        try {
            String groupName = name == null ? "" : name.trim();
            if (groupName.isBlank()) {
                return "Group name is required.";
            }
            String parentId = this.requireExistingGroupId(parentGroupId);
            boolean duplicate = ShellGroupStore.INSTANCE.selectList().stream()
                    .anyMatch(group -> Objects.equals(group.getPid(), parentId)
                            && groupName.equalsIgnoreCase(this.value(group.getName())));
            if (duplicate) {
                return "A sibling group with the same name already exists: " + groupName;
            }
            ShellGroup group = new ShellGroup();
            group.setName(groupName);
            group.setPid(parentId);
            group.setExpand(true);
            if (!ShellGroupStore.INSTANCE.insert(group)) {
                return "Group creation failed.";
            }
            ShellEventUtil.groupAdded(groupName);
            ShellEventUtil.dataImported();
            return "Created group:\nid: " + this.value(group.getGid())
                    + "\nname: " + groupName
                    + "\nparentId: " + this.value(parentId);
        } catch (Exception exception) {
            return this.failure("Create group", exception);
        }
    }

    @Tool(name = "renameGroup", value = "Rename one existing EasyShell connection group without changing its hierarchy or connections")
    public String renameGroup(
            @P(name = "groupId", value = "Group id returned by listGroups") String groupId,
            @P(name = "newName", value = "New non-empty group name") String newName) {
        try {
            String name = newName == null ? "" : newName.trim();
            if (name.isBlank()) {
                return "New group name is required.";
            }
            ShellGroup group = this.findGroup(groupId);
            if (group == null) {
                return "Group not found: " + this.value(groupId);
            }
            String oldName = group.getName();
            group.setName(name);
            if (!ShellGroupStore.INSTANCE.replace(group)) {
                return "Group rename failed.";
            }
            ShellEventUtil.groupRenamed(name, oldName);
            ShellEventUtil.dataImported();
            return "Renamed group:\nid: " + this.value(group.getGid())
                    + "\nname: " + name
                    + "\noldName: " + this.value(oldName);
        } catch (Exception exception) {
            return this.failure("Rename group", exception);
        }
    }

    @Tool(name = "createSnippet", value = "Create and persist a new terminal snippet; existing snippets are not modified or deleted")
    public String createSnippet(
            @P(name = "name", value = "Snippet name") String name,
            @P(name = "content", value = "Snippet command content") String content) {
        try {
            String snippetName = name == null ? "" : name.trim();
            if (snippetName.isBlank()) {
                return "Snippet name is required.";
            }
            if (content == null || content.isBlank()) {
                return "Snippet content is required.";
            }
            ShellSnippet snippet = new ShellSnippet();
            snippet.setName(snippetName);
            snippet.setContent(content);
            if (!ShellSnippetStore.INSTANCE.insert(snippet)) {
                return "Snippet creation failed.";
            }
            return "Created snippet:\nid: " + this.value(snippet.getId())
                    + "\nname: " + snippetName
                    + "\ncontentLength: " + content.length();
        } catch (Exception exception) {
            return this.failure("Create snippet", exception);
        }
    }

    @Tool(name = "createQuery", value = "Create and persist a new saved query for an existing connection; existing queries are not modified or deleted")
    public String createQuery(
            @P(name = "connectionId", value = "Connection id returned by listSavedConnections") String connectionId,
            @P(name = "name", value = "Query name") String name,
            @P(name = "content", value = "Query text") String content,
            @P(name = "database", value = "Optional database or schema name", required = false) String database,
            @P(name = "dbIndex", value = "Optional Redis database index, default 0", required = false) Integer dbIndex) {
        try {
            ShellConnect connection = this.findConnection(connectionId);
            if (connection == null) {
                return "Connection not found: " + this.value(connectionId);
            }
            String queryName = name == null ? "" : name.trim();
            if (queryName.isBlank()) {
                return "Query name is required.";
            }
            if (content == null || content.isBlank()) {
                return "Query content is required.";
            }
            ShellQuery query = new ShellQuery();
            query.setIid(connection.getId());
            query.setName(queryName);
            query.setContent(content);
            if (database != null && !database.isBlank()) {
                query.setDbName(database.trim());
            }
            query.setDbIndex(dbIndex == null || dbIndex < 0 ? 0 : dbIndex);
            if (!ShellQueryStore.INSTANCE.insert(query)) {
                return "Query creation failed.";
            }
            return "Created query:\nid: " + this.value(query.getUid())
                    + "\nname: " + queryName
                    + "\nconnectionId: " + this.value(connection.getId())
                    + "\ndatabase: " + this.value(query.getDbName())
                    + "\ndbIndex: " + query.getDbIndex()
                    + "\ncontentLength: " + content.length();
        } catch (Exception exception) {
            return this.failure("Create query", exception);
        }
    }

    @Tool(name = "getDatabaseSupportInfo", value = "Get supported database and NoSQL types plus bundled client driver versions")
    public String getDatabaseSupportInfo() {
        StringBuilder result = new StringBuilder();
        result.append("localStore: SQLite\n");
        result.append("mysql: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("com.mysql.cj.conf.PropertyDefinitions")).append('\n');
        result.append("mariadb: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("org.mariadb.jdbc.Driver")).append('\n');
        result.append("dameng: type=SQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("dm.jdbc.driver.DmDriver")).append('\n');
        result.append("mongodb: type=NoSQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("com.mongodb.client.MongoClient")).append('\n');
        result.append("redis: type=NoSQL | liveVersionAvailable=true | driverVersion=")
                .append(this.packageVersion("redis.clients.jedis.JedisPooled")).append('\n');
        result.append("zookeeper: type=Coordination | liveVersionAvailable=false | driverVersion=")
                .append(this.packageVersion("org.apache.zookeeper.ZooKeeper")).append('\n');
        return result.toString();
    }

    @Tool(name = "getOpenDatabaseVersions", value = "Get database or NoSQL server versions for currently open connected tabs")
    public String getOpenDatabaseVersions(
            @P(name = "filter", value = "Optional case-insensitive connection name or type filter") String filter) {
        try {
            List<OpenConnectionSnapshot> connections = this.openConnections().stream()
                    .filter(connection -> this.isDatabaseClient(connection.client()))
                    .filter(connection -> this.matchesOpenConnection(connection, filter))
                    .toList();
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(connections.size()).append('\n');
            for (OpenConnectionSnapshot connection : connections) {
                result.append("name: ").append(this.value(connection.connect().getName()))
                        .append(" | type: ").append(this.value(connection.connect().getType()))
                        .append(" | state: ").append(this.value(connection.state()))
                        .append(" | serverVersion: ").append(this.value(this.databaseVersion(connection.client())))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get open database versions", exception);
        }
    }

    @Tool(name = "getApplicationSettingsSummary", value = "Get non-sensitive EasyShell application settings")
    public String getApplicationSettingsSummary() {
        try {
            ShellSetting setting = ShellSettingStore.SETTING;
            if (setting == null) {
                return "Application settings are unavailable.";
            }
            StringBuilder result = new StringBuilder();
            this.append(result, "theme", this.value(setting.getTheme()));
            this.append(result, "fontFamily", this.value(setting.getFontFamily()));
            this.append(result, "fontSize", setting.getFontSize());
            this.append(result, "fontWeight", setting.getFontWeight());
            this.append(result, "editorFontFamily", this.value(setting.getEditorFontFamily()));
            this.append(result, "editorFontSize", this.value(setting.getEditorFontSize()));
            this.append(result, "editorFontWeight", setting.getEditorFontWeight());
            this.append(result, "terminalFontFamily", this.value(setting.getTerminalFontFamily()));
            this.append(result, "terminalFontSize", this.value(setting.getTerminalFontSize()));
            this.append(result, "terminalFontWeight", setting.getTerminalFontWeight());
            this.append(result, "llmThinkingLevel", this.enumName(
                    LLMThinkingLevel.values(), setting.getLlmThinkingLevel()));
            this.append(result, "llmStreaming", setting.isLlmStreaming());
            this.append(result, "llmMaxMessages", setting.getLlmMaxMessages());
            this.append(result, "llmExecutionMode", this.enumName(
                    LLMExecutionMode.values(), setting.getLlmExecutionMode()));
            this.append(result, "terminalType", this.value(setting.getTermType()));
            this.append(result, "terminalMaxLineCount", this.value(
                    setting.getTermMaxLineCount() == null ? null : String.valueOf(setting.getTermMaxLineCount())));
            this.append(result, "terminalRefreshRate", this.value(
                    setting.getTermRefreshRate() == null ? null : String.valueOf(setting.getTermRefreshRate())));
            this.append(result, "terminalCopyOnSelected", setting.isTermCopyOnSelected());
            this.append(result, "terminalUseAntialiasing", setting.isTermUseAntialiasing());
            this.append(result, "terminalParseHyperlink", setting.isTermParseHyperlink());
            this.append(result, "redisKeyLoadLimit", setting.getKeyLoadLimit());
            this.append(result, "redisRowPageLimit", setting.getRowPageLimit());
            this.append(result, "recordPageLimit", setting.getRecordPageLimit());
            this.append(result, "mongoRecordPageLimit", setting.getMongoRecordPageLimit());
            this.append(result, "shortcutKeyEnabled", setting.isEnableShortcutKey());
            this.append(result, "graphicMode", setting.getGraphicMode());
            this.append(result, "x11Port", setting.getX11Port());
            this.append(result, "x11Glx", setting.isX11Glx());
            this.append(result, "x11Local", setting.isX11Local());
            this.append(result, "x11FocusOnClick", setting.isX11FocusOnClick());
            this.append(result, "x11SingleWindow", setting.isX11SingleWindow());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get application settings", exception);
        }
    }

    @Tool(name = "listThemes", value = "List available application themes")
    public String listThemes(
            @P(name = "filter", value = "Optional case-insensitive theme name or description filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<ThemeStyle> themes = new ArrayList<>(Themes.allThemes());
            themes.sort(Comparator.comparing(
                    theme -> theme.getDesc(Locale.getDefault()), String.CASE_INSENSITIVE_ORDER));
            List<ThemeStyle> matched = themes.stream()
                    .filter(theme -> query.isBlank()
                            || this.value(theme.getName()).toLowerCase(Locale.ROOT).contains(query)
                            || this.value(theme.getDesc(Locale.getDefault())).toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(matched.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(matched.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                result.append(this.themeSummary(matched.get(index))).append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List themes", exception);
        }
    }

    @Tool(name = "getCurrentTheme", value = "Get the currently applied application theme and colors")
    public String getCurrentTheme() {
        try {
            return this.themeSummary(ThemeManager.currentTheme());
        } catch (Exception exception) {
            return this.failure("Get current theme", exception);
        }
    }

    @Tool(name = "setTheme", value = "Change and persist the application theme")
    public String setTheme(
            @P(name = "themeName", value = "Exact theme name returned by listThemes") String themeName) {
        try {
            if (themeName == null || themeName.isBlank()) {
                return "Theme name is required.";
            }
            ThemeStyle theme = Themes.getTheme(themeName.trim());
            if (theme == null) {
                return "Theme not found: " + themeName;
            }
            this.applyTheme(theme);
            return "Applied theme:\n" + this.themeSummary(theme);
        } catch (Exception exception) {
            return this.failure("Set theme", exception);
        }
    }

    @Tool(name = "toggleTheme", value = "Switch between the current theme and its light or dark counterpart")
    public String toggleTheme() {
        try {
            ThemeStyle current = ThemeManager.currentTheme();
            ThemeStyle target = ThemeUtil.getInverseTheme(current);
            if (target == null) {
                return "No inverse theme is available.";
            }
            this.applyTheme(target);
            return "Applied theme:\n" + this.themeSummary(target);
        } catch (Exception exception) {
            return this.failure("Toggle theme", exception);
        }
    }

    @Tool(name = "listFonts", value = "List system fonts available to JavaFX")
    public String listFonts(
            @P(name = "filter", value = "Optional case-insensitive font family filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        try {
            String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
            List<String> fonts = Font.getFamilies().stream()
                    .filter(font -> query.isBlank() || font.toLowerCase(Locale.ROOT).contains(query))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            int limit = this.normalizeLimit(maxResults, 200, 1000);
            int shown = Math.min(fonts.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(fonts.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                result.append(fonts.get(index)).append('\n');
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("List fonts", exception);
        }
    }

    @Tool(name = "getCurrentFont", value = "Get current application, editor, and terminal font settings")
    public String getCurrentFont() {
        try {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font current = FontManager.currentFont();
            StringBuilder result = new StringBuilder();
            this.append(result, "appliedName", current.getName());
            this.append(result, "appliedFamily", current.getFamily());
            this.append(result, "appliedSize", current.getSize());
            if (setting != null) {
                this.appendFontConfig(result, "application", setting.fontConfig());
                this.appendFontConfig(result, "editor", setting.editorFontConfig());
                this.appendFontConfig(result, "terminal", setting.terminalFontConfig());
            }
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get current font", exception);
        }
    }

    @Tool(name = "setApplicationFont", value = "Change and persist the main application font")
    public String setApplicationFont(
            @P(name = "fontFamily", value = "Exact font family returned by listFonts") String fontFamily,
            @P(name = "fontSize", value = "Font size from 8 to 72") int fontSize,
            @P(name = "fontWeight", value = "Font weight from 100 to 900 in steps of 100") int fontWeight) {
        try {
            if (fontFamily == null || fontFamily.isBlank()) {
                return "Font family is required.";
            }
            String matchedFamily = Font.getFamilies().stream()
                    .filter(family -> family.equalsIgnoreCase(fontFamily.trim()))
                    .findFirst()
                    .orElse(null);
            if (matchedFamily == null) {
                return "Font family not found: " + fontFamily;
            }
            if (fontSize < 8 || fontSize > 72) {
                return "Font size must be between 8 and 72.";
            }
            if (fontWeight < 100 || fontWeight > 900 || fontWeight % 100 != 0) {
                return "Font weight must be 100, 200, 300, 400, 500, 600, 700, 800, or 900.";
            }
            ShellSetting setting = ShellSettingStore.SETTING;
            setting.setFontFamily(matchedFamily);
            setting.setFontSize((byte) fontSize);
            setting.setFontWeight((short) fontWeight);
            ShellSettingStore.INSTANCE.replace(setting);
            FXUtil.runWait(() -> FontManager.apply(setting.fontConfig()));
            return "Applied application font:\n" + this.fontConfigSummary("application", setting.fontConfig());
        } catch (Exception exception) {
            return this.failure("Set application font", exception);
        }
    }

    @Tool(name = "getDependencyVersions", value = "Get JavaFX, LangChain4j, database driver, and other bundled dependency versions")
    public String getDependencyVersions() {
        Map<String, String> versions = new LinkedHashMap<>();
        versions.put("javaFx", this.packageVersion("javafx.application.Application"));
        versions.put("langChain4j", this.packageVersion("dev.langchain4j.service.AiServices"));
        versions.put("mysqlDriver", this.packageVersion("com.mysql.cj.conf.PropertyDefinitions"));
        versions.put("mariaDbDriver", this.packageVersion("org.mariadb.jdbc.Driver"));
        versions.put("damengDriver", this.packageVersion("dm.jdbc.driver.DmDriver"));
        versions.put("mongoDriver", this.packageVersion("com.mongodb.client.MongoClient"));
        versions.put("jedis", this.packageVersion("redis.clients.jedis.JedisPooled"));
        versions.put("zookeeper", this.packageVersion("org.apache.zookeeper.ZooKeeper"));
        versions.put("sqlite", this.packageVersion("org.sqlite.JDBC"));
        versions.put("h2", this.packageVersion("org.h2.Driver"));
        StringBuilder result = new StringBuilder();
        versions.forEach((name, version) -> this.append(result, name, version));
        return result.toString();
    }

    @Tool(name = "executeLocalCommandDirect",
            value = "Directly execute a command on the local machine running EasyShell")
    public String executeLocalCommand(
            @P(name = "command", value = "The command to execute") String command) {
        return this.executeLocalCommand(command, null, ShellLocalCommandExecutor.DEFAULT_TIMEOUT_SECONDS);
    }

    @Tool(name = "executeLocalCommandInDirectoryDirect",
            value = "Directly execute a command in a specific local working directory")
    public String executeLocalCommandInDirectory(
            @P(name = "workingDirectory", value = "Absolute or relative local working directory") String workingDirectory,
            @P(name = "command", value = "The command to execute") String command,
            @P(name = "timeoutSeconds", value = "Timeout in seconds, maximum 600") long timeoutSeconds) {
        return this.executeLocalCommand(command, workingDirectory, timeoutSeconds);
    }

    private String executeLocalCommand(String command, String workingDirectory, long timeoutSeconds) {
        try {
            return this.localCommandExecutor.execute(command, workingDirectory, timeoutSeconds).format();
        } catch (Exception exception) {
            return this.failure("Execute local command", exception);
        }
    }

    @Tool(name = "getXServerStatus", value = "Get built-in X server status and non-sensitive X11 settings")
    public String getXServerStatus() {
        try {
            int statusCode = ShellX11Manager.xServerStatus();
            String status = switch (statusCode) {
                case 0 -> "NOT_STARTED";
                case 1 -> "RUNNING";
                case 2 -> "STOPPED";
                default -> "UNKNOWN";
            };
            ShellSetting setting = ShellSettingStore.SETTING;
            StringBuilder result = new StringBuilder();
            this.append(result, "statusCode", statusCode);
            this.append(result, "status", status);
            this.append(result, "clients", ShellX11Manager.xServerClients());
            this.append(result, "port", setting == null ? null : setting.getX11Port());
            this.append(result, "glx", setting == null ? null : setting.isX11Glx());
            this.append(result, "local", setting == null ? null : setting.isX11Local());
            this.append(result, "focusOnClick", setting == null ? null : setting.isX11FocusOnClick());
            this.append(result, "singleWindow", setting == null ? null : setting.isX11SingleWindow());
            return result.toString();
        } catch (Exception exception) {
            return this.failure("Get X server status", exception);
        }
    }

    @Tool(name = "startXServer", value = "Start EasyShell's built-in X server")
    public String startXServer() {
        try {
            ShellX11Manager.startXServer_built_in();
            return "X server start completed.";
        } catch (Exception exception) {
            return this.failure("Start X server", exception);
        }
    }

    @Tool(name = "stopXServer", value = "Stop EasyShell's built-in X server")
    public String stopXServer() {
        try {
            ShellX11Manager.stopXServer_built_in();
            return "X server stopped.";
        } catch (Exception exception) {
            return this.failure("Stop X server", exception);
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        return this.toolSystemMessage() + "\n\n"
                + LLMUtil.languageSystemMessage() + "\n\n"
                + LLMUtil.executionSystemMessage(executionMode);
    }

    @Override
    public void close() {
        this.localCommandExecutor.close();
    }

    public String toolSystemMessage() {
        String base = LLMUtil.readLlmText("internal_tool.txt");
        Project project = Project.load();
        String environment = LLMUtil.readLlmText("internal_environment.txt")
                .replace("{projectName}", this.value(project == null ? null : project.getName()))
                .replace("{projectVersion}", this.value(project == null ? null : project.getVersion()))
                .replace("{projectType}", this.value(project == null ? null : project.getType()))
                .replace("{jdkVersion}", this.value(Runtime.version().toString()))
                .replace("{os}", this.value(System.getProperty("os.name")))
                .replace("{storePath}", this.value(ShellConst.getStorePath()));
        return base + "\n\n" + environment;
    }

    private void append(StringBuilder result, String name, Object value) {
        result.append(name).append(": ").append(this.value(value)).append('\n');
    }

    private void appendCount(StringBuilder result, String name, CountSource source) {
        try {
            this.append(result, name, source.get());
        } catch (Exception exception) {
            this.append(result, name, "unavailable (" + this.error(exception) + ")");
        }
    }

    private String property(String name) {
        return this.value(System.getProperty(name));
    }

    private String describePath(Path path) {
        try {
            boolean exists = Files.exists(path);
            boolean directory = Files.isDirectory(path);
            boolean regularFile = Files.isRegularFile(path);
            String size = regularFile ? this.formatBytes(Files.size(path)) : "-";
            return path + " | exists=" + exists + " | directory=" + directory
                    + " | regularFile=" + regularFile + " | size=" + size;
        } catch (Exception exception) {
            return path + " | error=" + this.error(exception);
        }
    }

    private String enumName(Enum<?>[] values, int ordinal) {
        if (ordinal < 0 || ordinal >= values.length) {
            return "UNKNOWN";
        }
        return values[ordinal].name();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        String[] units = {"KiB", "MiB", "GiB", "TiB"};
        int unitIndex = -1;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.ROOT, "%.2f %s", value, units[unitIndex]);
    }

    private String failure(String action, Exception exception) {
        return action + " failed: " + this.error(exception);
    }

    private String error(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private String value(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "-" : text;
    }

    private Map<String, String> groupNames() {
        Map<String, String> result = new LinkedHashMap<>();
        for (ShellGroup group : ShellGroupStore.INSTANCE.selectList()) {
            result.put(group.getGid(), group.getName());
        }
        return result;
    }

    private Map<String, String> connectionNames() {
        Map<String, String> result = new LinkedHashMap<>();
        for (ShellConnect connection : ShellConnectStore.INSTANCE.selectList()) {
            result.put(connection.getId(), connection.getName());
        }
        return result;
    }

    private ShellSnippet findSnippet(String snippetId) {
        if (snippetId == null || snippetId.isBlank()) {
            return null;
        }
        String id = snippetId.trim();
        return ShellSnippetStore.INSTANCE.selectList().stream()
                .filter(snippet -> Objects.equals(snippet.getId(), id))
                .findFirst()
                .orElse(null);
    }

    private ShellQuery findQuery(String queryId) {
        if (queryId == null || queryId.isBlank()) {
            return null;
        }
        String id = queryId.trim();
        return ShellQueryStore.INSTANCE.selectList().stream()
                .filter(query -> Objects.equals(query.getUid(), id))
                .findFirst()
                .orElse(null);
    }

    private int messageCount(LLMSession session) {
        try {
            return session.getMessageCount();
        } catch (Exception ignored) {
            return -1;
        }
    }

    private ShellConnect findConnection(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            return null;
        }
        return ShellConnectStore.INSTANCE.selectOne(connectionId.trim());
    }

    private ShellConnect findFullConnection(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            return null;
        }
        String id = connectionId.trim();
        return ShellConnectStore.INSTANCE.loadFull().stream()
                .filter(connection -> Objects.equals(connection.getId(), id))
                .findFirst()
                .orElse(null);
    }

    private ShellGroup findGroup(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return null;
        }
        String id = groupId.trim();
        ShellGroup group = ShellGroupStore.INSTANCE.selectOne(id);
        if (group != null) {
            return group;
        }
        return ShellGroupStore.INSTANCE.selectList().stream()
                .filter(item -> Objects.equals(item.getGid(), id))
                .findFirst()
                .orElse(null);
    }

    private String requireExistingGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return null;
        }
        String id = groupId.trim();
        if (!ShellGroupStore.INSTANCE.exist(id)) {
            throw new IllegalArgumentException("Group not found: " + id);
        }
        return id;
    }

    private void applyTypeOptions(ShellConnect connection, String type, String rawHost, String typeOptions) {
        JSONObject options = null;
        if (typeOptions != null && !typeOptions.isBlank()) {
            try {
                options = JSON.parseObject(typeOptions);
            } catch (Exception exception) {
                throw new IllegalArgumentException("typeOptions must be a valid JSON object: " + this.error(exception));
            }
            if (options == null) {
                throw new IllegalArgumentException("typeOptions must be a JSON object.");
            }
            Set<String> supportedKeys = Set.of(
                    "osType", "termType", "backspaceType", "altSendsEscape", "environment", "ftpPassiveMode", "showHiddenFile",
                    "region", "s3Type", "smbShareName", "domain", "mongoAuthDatabase",
                    "mongoSpecifiedDatabase", "executeTimeoutSeconds", "serialPortName", "serialBaudRate",
                    "serialDataBits", "serialStopBits", "serialParity", "serialFlowControl",
                    "saslAuth", "sessionTimeoutMinutes", "zkCompatibility", "forwardAgent", "x11forwarding", "showFile",
                    "serverMonitor", "followTerminalDir", "enableCompress", "enableZModem");
            for (String key : options.keySet()) {
                if (!supportedKeys.contains(key)) {
                    throw new IllegalArgumentException("Unsupported typeOptions key: " + key);
                }
            }
        }
        if (options == null) {
            options = new JSONObject();
        }
        this.applyStringOption(options, "osType", connection::setOsType);
        this.applyStringOption(options, "termType", connection::setTermType);
        this.applyStringOption(options, "environment", connection::setEnvironment);
        this.applyStringOption(options, "region", connection::setRegion);
        this.applyStringOption(options, "domain", connection::setDomain);
        this.applyStringOption(options, "mongoAuthDatabase", connection::setMongoAuthDatabase);
        this.applyStringOption(options, "mongoSpecifiedDatabase", connection::setMongoSpecifiedDatabase);
        this.applyBooleanOption(options, "ftpPassiveMode", connection::setFtpPassiveMode);
        this.applyBooleanOption(options, "showHiddenFile", connection::setShowHiddenFile);
        this.applyBooleanOption(options, "altSendsEscape", connection::setAltSendsEscape);
        this.applyBooleanOption(options, "saslAuth", connection::setSaslAuth);
        this.applyBooleanOption(options, "forwardAgent", connection::setForwardAgent);
        this.applyBooleanOption(options, "x11forwarding", connection::setX11forwarding);
        this.applyBooleanOption(options, "showFile", connection::setShowFile);
        this.applyBooleanOption(options, "serverMonitor", connection::setServerMonitor);
        this.applyBooleanOption(options, "followTerminalDir", connection::setFollowTerminalDir);
        this.applyBooleanOption(options, "enableCompress", connection::setEnableCompress);
        this.applyBooleanOption(options, "enableZModem", connection::setEnableZModem);
        if (options.containsKey("executeTimeoutSeconds")) {
            int value = options.getIntValue("executeTimeoutSeconds");
            if (value <= 0) {
                throw new IllegalArgumentException("executeTimeoutSeconds must be positive.");
            }
            connection.setExecuteTimeOut(value);
        }
        if (options.containsKey("backspaceType")) {
            int value = options.getIntValue("backspaceType");
            if (value < 0 || value > 2) {
                throw new IllegalArgumentException("backspaceType must be between 0 and 2.");
            }
            connection.setBackspaceType(value);
        }
        if (options.containsKey("zkCompatibility")) {
            int value = options.getIntValue("zkCompatibility");
            if (value <= 0) {
                throw new IllegalArgumentException("zkCompatibility must be positive.");
            }
            connection.setCompatibility(value);
        }
        if (options.containsKey("sessionTimeoutMinutes")) {
            int value = options.getIntValue("sessionTimeoutMinutes");
            if (value <= 0) {
                throw new IllegalArgumentException("sessionTimeoutMinutes must be positive.");
            }
            connection.setSessionTimeOut(value);
        }
        if (type.equals("s3")) {
            String s3Type = this.firstNonBlank(this.optionString(options, "s3Type"), "S3");
            connection.setS3Type(this.normalizeS3Type(s3Type));
        }
        if (type.equals("smb")) {
            String shareName = this.firstNonBlank(this.optionString(options, "smbShareName"),
                    connection.getSmbShareName());
            if (shareName == null || shareName.isBlank()) {
                throw new IllegalArgumentException("smbShareName is required for an SMB connection.");
            }
            connection.setSmbShareName(shareName);
        }
        if (type.equals("serial")) {
            String portName = this.firstNonBlank(this.optionString(options, "serialPortName"), rawHost);
            if (portName == null || portName.isBlank()) {
                throw new IllegalArgumentException("serialPortName is required for a serial connection.");
            }
            int baudRate = options.containsKey("serialBaudRate") ? options.getIntValue("serialBaudRate") : 9600;
            int dataBits = options.containsKey("serialDataBits") ? options.getIntValue("serialDataBits") : 8;
            if (baudRate <= 0 || baudRate > 4_000_000) {
                throw new IllegalArgumentException("serialBaudRate must be between 1 and 4000000.");
            }
            if (dataBits < 5 || dataBits > 8) {
                throw new IllegalArgumentException("serialDataBits must be between 5 and 8.");
            }
            connection.setSerialPortName(portName);
            connection.setSerialBaudRate(baudRate);
            connection.setSerialNumDataBits(dataBits);
            connection.setSerialNumStopBits(this.resolveSerialStopBits(
                    this.firstNonBlank(this.optionString(options, "serialStopBits"), "1")));
            connection.setSerialParityBits(this.resolveSerialParity(
                    this.firstNonBlank(this.optionString(options, "serialParity"), "none")));
            connection.setSerialFlowControl(this.resolveSerialFlowControl(
                    this.firstNonBlank(this.optionString(options, "serialFlowControl"), "none")));
        }
        if (type.equals("mongodb") && (connection.getMongoAuthDatabase() == null || connection.getMongoAuthDatabase().isBlank())
                && connection.getUser() != null && !connection.getUser().isBlank()) {
            connection.setMongoAuthDatabase("admin");
        }
    }

    private void applyStringOption(JSONObject options, String key, java.util.function.Consumer<String> setter) {
        String value = this.optionString(options, key);
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private void applyBooleanOption(JSONObject options, String key, java.util.function.Consumer<Boolean> setter) {
        if (options.containsKey(key)) {
            setter.accept(options.getBoolean(key));
        }
    }

    private String optionString(JSONObject options, String key) {
        if (!options.containsKey(key) || options.get(key) == null) {
            return null;
        }
        String value = options.getString(key);
        return value == null ? null : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String normalizeS3Type(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "minio" -> "Minio";
            case "alibaba", "aliyun", "oss" -> "Alibaba";
            case "tencent", "cos" -> "Tencent";
            case "huawei", "obs" -> "Huawei";
            case "s3", "standard" -> "S3";
            default -> throw new IllegalArgumentException("Unsupported s3Type: " + value);
        };
    }

    private int resolveSerialStopBits(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "1", "one" -> SerialPort.ONE_STOP_BIT;
            case "1.5", "one_point_five", "one-point-five" -> SerialPort.ONE_POINT_FIVE_STOP_BITS;
            case "2", "two" -> SerialPort.TWO_STOP_BITS;
            default -> throw new IllegalArgumentException("Unsupported serialStopBits: " + value);
        };
    }

    private int resolveSerialParity(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "none", "no" -> SerialPort.NO_PARITY;
            case "even" -> SerialPort.EVEN_PARITY;
            case "odd" -> SerialPort.ODD_PARITY;
            case "mark" -> SerialPort.MARK_PARITY;
            case "space" -> SerialPort.SPACE_PARITY;
            default -> throw new IllegalArgumentException("Unsupported serialParity: " + value);
        };
    }

    private int resolveSerialFlowControl(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "none", "disabled" -> SerialPort.FLOW_CONTROL_DISABLED;
            case "rtscts", "rts/cts", "rts", "cts" -> SerialPort.FLOW_CONTROL_RTS_ENABLED
                    | SerialPort.FLOW_CONTROL_CTS_ENABLED;
            case "xonxoff", "xon/xoff" -> SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED
                    | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
            default -> throw new IllegalArgumentException("Unsupported serialFlowControl: " + value);
        };
    }

    private String normalizeConnectionType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Connection type is required.");
        }
        return switch (type.trim().toLowerCase(Locale.ROOT)) {
            case "ssh" -> "ssh";
            case "sftp" -> "sftp";
            case "ftp" -> "ftp";
            case "s3", "minio" -> "s3";
            case "webdav", "web-dav" -> "webdav";
            case "smb" -> "smb";
            case "local" -> "local";
            case "telnet" -> "telnet";
            case "serial" -> "serial";
            case "rlogin" -> "rlogin";
            case "mosh" -> "mosh";
            case "rdp" -> "rdp";
            case "vnc" -> "vnc";
            case "mysql" -> "mysql";
            case "maria", "mariadb" -> "mariadb";
            case "dm", "dameng" -> "dameng";
            case "sqlite" -> "sqlite";
            case "mongo", "mongodb" -> "mongodb";
            case "redis" -> "redis";
            case "zk", "zookeeper" -> "zookeeper";
            default -> throw new IllegalArgumentException("Unsupported connection type: " + type);
        };
    }

    private String buildConnectionHost(String host, Integer port, String type) {
        if (type.equals("local") || type.equals("serial") || type.equals("sqlite")) {
            return host;
        }
        if (port == null || port <= 0 || this.hasExplicitPort(host)) {
            return host;
        }
        return host + ":" + port;
    }

    private boolean hasExplicitPort(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        if (host.startsWith("[")) {
            int bracket = host.indexOf(']');
            return bracket > 0 && bracket + 1 < host.length() && host.charAt(bracket + 1) == ':';
        }
        int firstColon = host.indexOf(':');
        int lastColon = host.lastIndexOf(':');
        if (firstColon <= 0 || firstColon != lastColon || firstColon + 1 >= host.length()) {
            return false;
        }
        for (int index = firstColon + 1; index < host.length(); index++) {
            if (!Character.isDigit(host.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private String connectionSummary(ShellConnect connection, Map<String, String> groupNames) {
        StringBuilder result = new StringBuilder();
        this.append(result, "id", connection.getId());
        this.append(result, "name", connection.getName());
        this.append(result, "type", connection.getType());
        this.append(result, "host", connection.getHost());
        this.append(result, "port", connection.hostPort());
        this.append(result, "group", groupNames.get(connection.getGroupId()));
        this.append(result, "authConfigured", this.hasAuthentication(connection));
        this.append(result, "readonly", connection.isReadonly());
        this.append(result, "remark", connection.getRemark());
        return result.toString();
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private String truncate(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        int limit = maxChars <= 0 ? 65536 : Math.min(maxChars, 262144);
        return value.length() <= limit ? value : value.substring(0, limit) + "\n... content truncated";
    }

    private boolean matchesConnection(ShellConnect connection, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String query = filter.trim().toLowerCase(Locale.ROOT);
        return this.value(connection.getName()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getType()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getHost()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.getRemark()).toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean matchesOpenConnection(OpenConnectionSnapshot connection, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String query = filter.trim().toLowerCase(Locale.ROOT);
        return this.value(connection.title()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getName()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getType()).toLowerCase(Locale.ROOT).contains(query)
                || this.value(connection.connect().getHost()).toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean hasAuthentication(ShellConnect connection) {
        return !this.value(connection.getPassword()).equals("-")
                || !this.value(connection.getKeyId()).equals("-")
                || !this.value(connection.getCertificate()).equals("-");
    }

    private List<OpenConnectionSnapshot> openConnections() {
        List<OpenConnectionSnapshot> result = new ArrayList<>();
        if (this.tabPane == null) {
            return result;
        }
        FXUtil.runWait(() -> {
            Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
            for (Tab tab : new ArrayList<>(this.tabPane.getTabs())) {
                if (!(tab instanceof ShellConnectTab connectTab)) {
                    continue;
                }
                ShellBaseClient client = connectTab.client();
                ShellConnect connect = connectTab.shellConnect();
                if (client == null || connect == null) {
                    continue;
                }
                result.add(new OpenConnectionSnapshot(
                        tab.getId(),
                        tab.getText(),
                        tab == selectedTab,
                        connect,
                        this.value(client.getState()),
                        client));
            }
        });
        return result;
    }

    private boolean isDatabaseClient(ShellBaseClient client) {
        return client instanceof ShellMysqlClient
                || client instanceof ShellMariadbClient
                || client instanceof ShellDamengClient
                || client instanceof ShellMongoClient
                || client instanceof ShellRedisClient;
    }

    private String databaseVersion(ShellBaseClient client) {
        try {
            if (client instanceof ShellMysqlClient mysqlClient) {
                return mysqlClient.selectVersion();
            }
            if (client instanceof ShellMariadbClient mariadbClient) {
                return mariadbClient.selectVersion();
            }
            if (client instanceof ShellDamengClient damengClient) {
                return damengClient.selectVersion();
            }
            if (client instanceof ShellMongoClient mongoClient) {
                return mongoClient.selectVersion();
            }
            if (client instanceof ShellRedisClient redisClient) {
                String info = redisClient.info("server");
                if (info != null) {
                    for (String line : info.split("\\R")) {
                        if (line.startsWith("redis_version:")) {
                            return line.substring(line.indexOf(':') + 1).trim();
                        }
                    }
                }
            }
        } catch (Exception exception) {
            return "unavailable (" + this.error(exception) + ")";
        }
        return "-";
    }

    private int normalizeLimit(int requested, int defaultLimit, int maxLimit) {
        if (requested <= 0) {
            return defaultLimit;
        }
        return Math.min(requested, maxLimit);
    }

    private String packageVersion(String className) {
        try {
            Class<?> type = Class.forName(className, false, ShellLLMTools.class.getClassLoader());
            Package javaPackage = type.getPackage();
            if (javaPackage == null) {
                return "-";
            }
            String implementationVersion = javaPackage.getImplementationVersion();
            if (implementationVersion != null && !implementationVersion.isBlank()) {
                return implementationVersion;
            }
            URL location = type.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return "-";
            }
            String fileName = Path.of(location.toURI()).getFileName().toString();
            Matcher matcher = Pattern.compile("-(\\d+(?:\\.\\d+)+)(?:[-+][A-Za-z0-9._-]+)?\\.jar$").matcher(fileName);
            return matcher.find() ? matcher.group(1) : "-";
        } catch (Exception exception) {
            return "-";
        }
    }

    private String themeSummary(ThemeStyle theme) {
        if (theme == null) {
            return "-";
        }
        return "name: " + this.value(theme.getName())
                + " | description: " + this.value(theme.getDesc(Locale.getDefault()))
                + " | dark: " + theme.isDarkMode()
                + " | background: " + this.value(theme.getBackgroundColorHex())
                + " | foreground: " + this.value(theme.getForegroundColorHex())
                + " | accent: " + this.value(theme.getAccentColorHex());
    }

    private void applyTheme(ThemeStyle theme) {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTheme(theme.getName());
        setting.setBgColor(theme.getBackgroundColorHex());
        setting.setFgColor(theme.getForegroundColorHex());
        setting.setAccentColor(theme.getAccentColorHex());
        ShellSettingStore.INSTANCE.replace(setting);
        FXUtil.runWait(() -> ThemeManager.apply(theme));
    }

    private void appendFontConfig(StringBuilder result, String name, FontConfig config) {
        result.append(this.fontConfigSummary(name, config)).append('\n');
    }

    private String fontConfigSummary(String name, FontConfig config) {
        return name + " | family: " + this.value(config == null ? null : config.getFamily())
                + " | size: " + this.value(config == null ? null : config.getSize())
                + " | weight: " + this.value(config == null ? null : config.getWeight());
    }

    private record OpenConnectionSnapshot(
            String tabId,
            String title,
            boolean selected,
            ShellConnect connect,
            String state,
            ShellBaseClient client) {
    }

    @FunctionalInterface
    private interface CountSource {
        int get() throws Exception;
    }
}
